(ns tfevent-sink.event-io
  (:require [flatland.protobuf.core :refer :all]
            [com.rpl.specter :as sp]
            [clojure.java.io :as io])
  (:import [org.tensorflow.framework Summary Summary$Value Summary$Builder Summary$Value$Builder]
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

(defn spit-events
  "Given an output stream and seq of events, writes the bytearray form of the events
  to the stream"
  [^DataOutputStream output-stream events]
  (let [tw (new TFRecordWriter output-stream)]
    (doseq [x (mapv #(.toByteArray %) events)]
      (.write tw x))))

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

(defn event-stream
  "returns a new DataOutputStream initialized with the TFrecord header event"
  [file-path]
  (let [dos (new DataOutputStream (io/output-stream (io/file file-path)))]
    (reset-state)
    (spit-events dos [(start-event)])
    dos))

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

(defn make-event
  "returns an Event object with string tag and double value"
  [^String tag ^double v]
  (let [{:keys [:step :wall-time]} (get-tag-state tag)
        sum1 (doto
              (Summary/newBuilder)
               (.addValue
                (.build (doto (Summary$Value/newBuilder)
                          (.setTag tag)
                          (.setSimpleValue v)))))]
    (.build (doto
             (Event/newBuilder)
              (.setSummary sum1)
              (.setStep step)
              (.setWallTime wall-time)))))
