(ns tfevent-sink.event-io
  (:require [flatland.protobuf.core :refer :all]
            [com.rpl.specter :as sp]
            [clojure.java.io :as io]
            [tfevent-sink.histogram :refer :all]
            )
  (:import [org.tensorflow.framework Summary Summary$Value Summary$Builder Summary$Value$Builder HistogramProto]
           [org.tensorflow.util Event]
           [java.io DataInput File DataInputStream DataOutputStream]
           [org.tensorflow.hadoop.util TFRecordReader TFRecordWriter]))

(defn slurp-events
  "reads an event file and returns a seq of event objects, based on the load-fn provided"
  ([file-path] (slurp-events true file-path))
  ([compressed? file-path] (slurp-events #(Event/parseFrom %) compressed? file-path))
  ([load-fn compressed? file-path]
   (with-open [tp2 (new DataInputStream (io/input-stream (io/file file-path)))]
     (let [tfr (new TFRecordReader tp2 compressed?)]
       (loop [tfr tfr
              result []]
         (let [res (.read tfr)]
           (if (nil? res)
             result
             (recur tfr (conj result (load-fn res))))))))))

(defn- start-event
  "returns the first event in the event file, which is the wall time and
  fileversion with value brain.Event:2"
  []
  (.build
   (doto
    (Event/newBuilder)
     (.setWallTime (double (/ (.getTime (new java.util.Date)) 1000)))
     (.setFileVersion "brain.Event:2"))))

(defn append-events
  "Given an file path, appends the seq of events, in bytearray form to the file."
  [file-path events]
  (with-open [dos (new DataOutputStream (io/output-stream (io/file file-path) :append true))]
    (let [tw (new TFRecordWriter dos)]
      (doseq [x (mapv #(.toByteArray %) events)]
        (.write tw x)))))

;a state atom which records the start time and the steps for each tag
(def state-atom (atom {:wall-time 0
                       :tags {}}))

(defn- get-time
  "time in seconds since Epoch"
  []
  (double (/ (.getTime (new java.util.Date)) 1000)))

(defn- reset-state
  "resets the state"
  []
  (reset! state-atom {:wall-time (get-time)
                      :tags {}}))

(defn create-event-stream
  "Initializes the event stream with the TFrecord header event, and sets the starting wall time
  "
  [file-path]
  (do
    (reset-state)
    (io/delete-file file-path true)
    (append-events file-path [(start-event)])))

(defn- update-tag-state
  "adds a tag if it doesn't exist. If it does, update the step "
  [tag]
  (swap! state-atom #(if (get-in % [:tags tag])
                       (update-in % [:tags tag :step] inc)
                       (assoc-in % [:tags tag] {:step 0}))))

(defn- update-wall-time
  "set the initial wall time"
  [tag]
  (swap! state-atom #(assoc-in % [:wall-time]) (get-time)))

(defn- get-tag-state
  "returns a map with the next step and the wall time is the difference is the current
  time and start time"
  [tag]
  (let [cur-state (update-tag-state tag)]
    {:step (get-in cur-state [:tags tag :step])
     :wall-time (- (get-time) (get-in cur-state [:wall-time]))}))

(defn- summary
  [tag summary]
  (let [{:keys [:step :wall-time]} (get-tag-state tag)]
    (.build (doto
             (Event/newBuilder)
              (.setSummary summary)
              (.setStep step)
              (.setWallTime wall-time)))))

(defmulti make-event (fn ([x y] (mapv class [x y]))))

(defmethod make-event
  [String java.lang.Double]
  [tag v]
  (let [sum1 (doto
              (Summary/newBuilder)
               (.addValue
                (.build (doto (Summary$Value/newBuilder)
                          (.setTag tag)
                          (.setSimpleValue v)))))]
    (summary tag sum1)))

(defn build-hist
  [tag values]
  (let [{:keys [min max num sum sum-squares bucket-limit bucket]} 
        (make-histogram values)
        sum1 (doto
              (Summary/newBuilder)
               (.addValue
                (.build (doto (Summary$Value/newBuilder)
                          (.setTag tag)
                          (.setHisto (doto (HistogramProto/newBuilder)
                                       (.setMin min)
                                       (.setMax max)
                                       (.setNum num)
                                       (.setSum sum)
                                       (.setSumSquares sum-squares)
                                       (.addAllBucket bucket)
                                       (.addAllBucketLimit bucket-limit)))))))]
    sum1))

(defmethod make-event
  [String (class (double-array []))]
  [tag values]
  (let [sum1 (build-hist tag (vec values))]
    (summary tag sum1)))

(defmethod make-event
  [String clojure.lang.PersistentVector]
  [tag values]
  (let [;2 dimension matrix, vector containing N (rows) double-arrays
        ;flatten into 1 dimension vector
        sum1 (build-hist tag (->> values
                              (mapv vec)
                              (reduce into)))]
    (summary tag sum1)))
