(ns tfevent-sink.read-pb
  (:require [flatland.protobuf.core :refer :all]
                                  [com.rpl.specter :as sp]
                                  [clojure.java.io :as io]
                                  [tfevent-sink.histogram :refer :all]
                                  [tfevent-sink.event-io :as eio]
                                  )
    (:import [org.tensorflow.framework Summary Summary$Value Summary$Builder Summary$Value$Builder HistogramProto GraphDef MetaGraphDef SavedModel] 
             [org.tensorflow.util Event SaverDef]
             [java.io DataInput File DataInputStream DataOutputStream]
             [org.tensorflow.hadoop.util TFRecordReader TFRecordWriter]))

(def kpath "/home/kiran/Downloads/inception/tensorflow_inception_graph.pb")
(def k2 "/home/kiran/src/github/tensorflow/tensorflow/cc/saved_model/testdata/half_plus_two/00000123/saved_model.pb")
(def k3 "/home/kiran/.cache/bazel/_bazel_kiran/e403d7ec8e341bbaa08d8b006391d06d/external/inception5h/tensorflow_inception_graph.pb")
(def k4 "/tmp/events.out.tfevents.1489395796.1ebb18e90225")
(def k5 "/tmp/events.out.tfevents.1494555107.kiran-ThinkPad-L460")
(def k6 "/home/kiran/Downloads/my-model/my-model1.data-00000-of-00001")
(def k6meta "/home/kiran/Downloads/my-model/my-model1.meta")
(eio/slurp-events k4)
(def gdef (GraphDef/parseFrom (io/input-stream (io/file k4))))
(def gdef (GraphDef/parseFrom (io/input-stream (io/file kpath))))
(def mgdef (MetaGraphDef/parseFrom (io/input-stream (io/file k6meta))))
(def cmdef (.getCollectionDefMap mgdef))
(.getCollectionDefOrThrow mgdef "trainable_variables")
(map #(.getName %) (.getOpList (.getStrippedOpList (.getMetaInfoDef mgdef))))
(keys cmdef)
(def modvar (get cmdef "model_variables"))
(let [nl (.getNodeList modvar)]
  (.size nl))
(def gdef (.getGraphDef mgdef))
(count (.getNodeList gdef))
(class gdef)
(def nodes (.getNodeList gdef))
(take 10 (map #(.getOp %) nodes))
(take 10 (map #(.getInputCount %) nodes))
(map #(.getInputCount %) nodes)
(.getAttrMap gdef)
(count nodes)
(-> nodes first )
(.getVersions gdef )

(-> gdef)
(with-open [tp2 (new DataInputStream (io/input-stream (io/file file-path)))]
  (let [tfr (new TFRecordReader tp2 compressed?)]
    (loop [tfr tfr
           result []]
      (let [res (.read tfr)]
        (if (nil? res)
          result
          (recur tfr (conj result (load-fn res))))))))
