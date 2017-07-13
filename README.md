# tfevent-sink

A Clojure library that consumes scalar events (such as training and validation metrics) from a machine learning training cycle and outputs events to file in the TFRecord (Tensorflow record) format. The progress of training can be viewed with the Tensorboard tool packaged along with Tensorboard.

This has been tested with version 1.1 of Tensorflow (which uses version 3.1 of protobuf)

## Importing


[![Clojars Project](https://img.shields.io/clojars/v/org.shark8me/tfevent-sink.svg)](https://clojars.org/org.shark8me/tfevent-sink)


## Usage

### Example of logging scalar event (MSE) to a log file

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

### Load the file from Tensorboard

```bash

tensorboard --logdir="/tmp/"
``` 

Detailed instructions can be found [here](https://www.tensorflow.org/get_started/summaries_and_tensorboard)


## How to regenerate for newer version of Protobuf and/or Tensorflow

* Update the dependency on proto to the latest version. Link [on mvnrepository](mvnrepository.com/artifact/org.tensorflow/proto) 

## TODO

* Migrate from generated java code (using protoc) to clojure-protobuf. At the moment, clojure-protobuf doesn't create events properly

## License

Copyright © 2017 shark8me

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
