(ns tfevent-sink.histogram
  (:require [flatland.protobuf.core :refer :all]
            [com.rpl.specter :as sp]
            [clojure.core.matrix :as m]
            [clojure.core.matrix.stats :as mstats]
            [clojure.java.io :as io])
  (:import [org.tensorflow.framework Summary Summary$Value Summary$Builder Summary$Value$Builder HistogramProto]
           [org.tensorflow.util Event]))

;;values of min and max used from generate_testdata.py
(defn- hist-buckets
  []
  (let [imin 1E-12
        imax 1E20
        pos-buck (take-while #(< % 1E20) (iterate (partial * 1.1) 1E-12))
        neg-buck (mapv #(* % -1) pos-buck)]
    (vec (concat neg-buck [0] pos-buck))))

(def bucket-limits (hist-buckets))

(defn insertion-index
  [limits values]
  ;;if the value is not present binary search , (-(insertion point) - 1)
  ;;see https://docs.oracle.com/javase/7/docs/api/java/util/Arrays.html#binarySearch(double[],%20double)
  (m/emap #(let [x (java.util.Arrays/binarySearch limits %)]
             (if (neg? x) (* -1 (+ 1 x)) x))
          values))

(defn make-histogram
  [values]
  (let [in-index (insertion-index (double-array bucket-limits) values)
        kmap (reduce (fn [acc [k v]] (assoc acc k v))
                     (sorted-map) (frequencies in-index))
        ret-vals (->> kmap vals vec (mapv double))
        blim (->> (keys kmap) 
                  (mapv (comp double bucket-limits)))
        ]
    ;(println "blimc " values " " ret-vals " " (m/emin blim) " " (m/emax blim))
    {:min  (m/emin values)
     :max (m/emax values)
     :num (count values)
     :sum-squares (mstats/sum-of-squares values)
     :sum (m/esum values)
     :bucket-limit blim
     :bucket ret-vals}))
