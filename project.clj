(defproject org.shark8me/tfevent-sink "0.1.4"
  :description "Enables writing events from Clojure in Tensorflow format"
  :url "http://github.com/shark8me/tfevent-sink"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-paths [ ;sources copied from github.com/tensorflow/ecosystem for reading and writing TFRecords
                      "src/java-ecosystem" ]
  :prep-tasks  ["javac" "compile"]
  :source-paths ["src/clojure"]
  :test-paths ["test/clojure"]
  :signing  {:gpg-key "shark8me"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojars.ghaskins/protobuf "3.1.0-1"]
                 ;; https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
                 [com.google.protobuf/protobuf-java "3.1.0"]
                 ;; https://mvnrepository.com/artifact/org.tensorflow/proto
                 [org.tensorflow/proto "1.2.1"]
                 [net.mikera/core.matrix "0.60.3"]
                [com.rpl/specter "1.0.1"]]
  :javac-options ["-Xmaxerrs" "1000"]
  :javadoc-opts { :package-names ["org.tensorflow.framework"
                  "org.tensorflow.util"]}
  :repositories  [["releases"  {:url "https://clojars.org/repo"
                                :creds :gpg}]]
  :plugins  [[lein-protobuf "0.1.1"]
             [lein-javadoc "0.3.0"]]
  )
