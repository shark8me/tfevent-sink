# tfevent-sink

A Clojure library designed that consumes scalar events (such as training and validation metrics) from a machine learning training cycle and outputs events to file in the TFRecord (Tensorflow record) format. The progress of training can be viewed with the Tensorboard tool packaged along with Tensorboard.

This has been tested with version 1.1 of Tensorflow (which uses version 3.1 of protobuf)

## Usage

FIXME

## To

* Download the 3.1.1 release of protobuf java from Github (this version is used in tensorflow r1.1.)
* Build and install protoc, verify that "protoc --version" reflects the version just downloaded
* Copy the *.proto files from the tensorflow repo into this repo's /resources folder, including the file paths. 
* Run "protoc --java_out=src/java <insert paths to event.proto and summary.proto> " 
protoc --java_out=src/java/ --proto_path=resources/proto/ resources/proto/tensorflow/core/util/event.proto resources/proto/tensorflow/core/framework/summary.proto resources/proto/tensorflow/core/framework/tensor.proto resources/proto/tensorflow/core/framework/tensor_shape.proto resources/proto/tensorflow/core/framework/resource_handle.proto resources/proto/tensorflow/core/framework/types.proto

## License

Copyright © 2017 shark8me

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
