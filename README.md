# tfevent-sink

A Clojure library that consumes scalar events (such as training and validation metrics) from a machine learning training cycle and outputs events to file in the TFRecord (Tensorflow record) format. The progress of training can be viewed with the Tensorboard tool packaged along with Tensorboard.

This has been tested with version 1.1 of Tensorflow (which uses version 3.1 of protobuf)

## Usage

```clojure
(ns ;...
 (:require [tfevent-sink.event-io :as eio]))
 
(let [file-path "/tmp/run/tfevents.log"
      ;;create a scalar event with a name and value
      ev (eio/make-event "loss/mean-squared-error" (double 0.09))] 
   ;;create and initialize the event stream
  (eio/create-event-stream file-path)
  ;;append event to stream
  (eio/append-events file-path [ev])
  

```

## How to regenerate for newer version of Protobuf and/or Tensorflow

* Download the 3.1.1 release of protobuf java from Github (this version is used in tensorflow r1.1.)
* Build and install protoc, verify that "protoc --version" reflects the version just downloaded
* Copy the *.proto files from the tensorflow repo into this repo's /resources folder, including the file paths. 
* Run "protoc --java_out=src/java <insert paths to event.proto and summary.proto> " e.g.
protoc --java_out=src/java/ --proto_path=resources/proto/ resources/proto/tensorflow/core/util/event.proto resources/proto/tensorflow/core/framework/summary.proto resources/proto/tensorflow/core/framework/tensor.proto resources/proto/tensorflow/core/framework/tensor_shape.proto resources/proto/tensorflow/core/framework/resource_handle.proto resources/proto/tensorflow/core/framework/types.proto


## TODO

* Migrate from generated java code (using protoc) to clojure-protobuf. At the moment, clojure-protobuf doesn't create events properly
* Add API for displaying histograms
## License

Copyright © 2017 shark8me

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
