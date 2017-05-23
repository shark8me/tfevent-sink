(ns tfevent-sink.event-io-test
  (:require [flatland.protobuf.core :refer :all]
            [clojure.test :refer :all]
            [tfevent-sink.event-io :refer :all])
  (:import [org.tensorflow.util Event]))

(deftest tagtest
  (testing "testing tags and simple values"
    (let [fname (java.io.File/createTempFile "tfevents" ".out")
          event (protodef Event)
          tag "a/fdd2"
          _ (with-open [fos (event-stream fname)]
              (spit-events fos (mapv #(make-event  tag (double %)) (range 10))))
          resp (slurp-events #(protobuf-load event %) true fname)]
      ;is every tag the same value
      (is (every? (partial = tag)
                  (mapv #(get-in resp [(inc %) :summary :value 0 :tag]) (range 10))))
      ;is every simple value the same as the input
      (is (= (vec (mapv double (range 10)))
             (mapv #(get-in resp [(inc %) :summary :value 0 :simple-value]) (range 10))))
      ;steps should increment
      (is (= (vec (range 1 10))
             (mapv #(get-in resp [(inc %) :step]) (range 1 10)))))))
