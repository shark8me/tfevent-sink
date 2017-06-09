(ns tfevent-sink.event-io-test
  (:require [flatland.protobuf.core :refer :all]
            [clojure.test :refer :all]
            [tfevent-sink.event-io :refer :all]
            [tfevent-sink.histogram :refer :all]
            
            )
  (:import [org.tensorflow.util Event]))

(deftest eventtest
  (testing "double"
    (is (instance? Event (make-event "tagname" (double 0.8))))) 
  (testing "hist"
    (is (instance? Event (make-event "tagname" [(double-array (range 2 12))] )))
    (is (instance? Event (make-event "tagname" (double-array (range 2 12)) )))))

(deftest tagtest
  (testing "testing tags and simple values"
    (let [fname (java.io.File/createTempFile "tfevents" ".out")
          event (protodef Event)
          tag "a/fdd2"
          _ (create-event-stream fname)
          _ (append-events fname (mapv #(make-event  tag (double %)) (range 10)))
          resp (slurp-events #(protobuf-load event %) true fname)]
      ;is every tag the same value
      (is (every? (partial = tag)
                  (mapv #(get-in resp [(inc %) :summary :value 0 :tag]) (range 10))))
      ;is every simple value the same as the input
      (is (= (vec (mapv double (range 10)))
             (mapv #(get-in resp [(inc %) :summary :value 0 :simple-value]) (range 10))))
      ;steps should increment
      (is (= (vec (range 1 10))
             (mapv #(get-in resp [(inc %) :step]) (range 1 10))))))
  (testing "testing histograms"
    (let [fname (java.io.File/createTempFile "tfeventhist" ".out")
          event (protodef Event)
          tag "a/hist"
          _ (create-event-stream fname)
          _ (append-events fname (mapv #(make-event (str tag (str %)) (double-array (range % (+ % 10)))) (range 10)))
          resp (slurp-events #(protobuf-load event %) true fname)]
      ;is every tag the same value
      (is (= (mapv (partial str tag) (range 10)) 
                  (mapv #(get-in resp [(inc %) :summary :value 0 :tag]) (range 10))))
      (is (every? #(= 1.0 %)  (get-in resp [1 :summary :value 0 :histo :bucket])))
      )))
