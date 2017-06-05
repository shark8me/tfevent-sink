(ns tfevent-sink.hist-test
  (:require [flatland.protobuf.core :refer :all]
            [clojure.test :refer :all]
            [tfevent-sink.histogram :refer :all])
  (:import [org.tensorflow.util Event]))

(deftest histest
  (testing "valid bucket limits"
    (let [h (make-histogram (mapv double (range 2 12)))
          expected-buckets [2.1628190051144287, 3.1665833053880363, 4.214722379471477, 5.099814079160488, 6.1707750357841915, 7.466637793298873, 8.213301572628762, 9.034631729891638, 10.931904393168884, 12.025094832485772]]
      (is (every? zero? (mapv - (:bucket-limit h) expected-buckets)))
      (is (every? #(= 1 %) (:bucket h))))))

(deftest insertion
  (testing "valid insertion index ")
  (is (= (vec (range 10)) (insertion-index (double-array (range 2 12)) (mapv double (range 2 12)))))
  (is (= (vec (range 1 11)) (insertion-index (double-array (range 2 12)) (mapv double (range 2.1 12.1))))))
