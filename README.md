

# StartupOS

[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/google/startup-os)
[![CircleCI](https://circleci.com/gh/google/startup-os/tree/master.svg?style=svg)](https://circleci.com/gh/google/startup-os/tree/master)

> Examples for Google's Open Source stack and deployment to the cloud.

The main technologies in the stack are:
* [Angular](https://angular.io/)
* [Protol Buffers](https://developers.google.com/protocol-buffers/)
* [gRPC](https://grpc.io/)
* [Bazel](https://bazel.build/)

## Hands-on experience
Try the Google Cloud Shell tutorial on formatting code using a Bazel-built tool:

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/google/startup-os&page=shell&tutorial=tutorials/formatting-code.md)

Try the Google Cloud Shell tutorial on renaming proto fields:

[![Open in Cloud Shell](http://gstatic.com/cloudssh/images/open-btn.svg)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/google/startup-os&page=shell&tutorial=tutorials/proto_rename/renaming-proto.md)

## Supported languages

Protos, gRPC and Bazel are polyglot. The examples in this repo are mostly in Java and Typescript, but there's support for many other languages:
* gRPC and Protocol Buffers are supported by Google in C++, Java (and Android Java), Python, Go, C#, Objective-C, PHP, Dart, Ruby and JavaScript (incl. Node.js).
* Bazel is supported by Google in Java (incl. Android builds), Objective-C (incl. iOS builds), C++, Go, Dart, Rust, Sass and Scala.
* The community has added support for many others languages. See [this list](https://github.com/google/protobuf/blob/master/docs/third_party.md) for Protocol Buffers and gRPC, and [this one](https://github.com/jin/awesome-bazel#rules) for Bazel.

## Top examples
* Lots of [Protocol Buffer examples](https://github.com/search?utf8=%E2%9C%93&q=repo%3Agoogle%2Fstartup-os+extension%3Aproto&type=Code&ref=advsearch&l=&l=).
* [gRPC-Web](https://github.com/oferb/startup-os-example/tree/master/app): a js client library running in the browser, connected to a gRPC server through an HTTP proxy. Both server and client use gRPC auto-generated stubs to handle communication.
* gRPC [microservices example](https://github.com/google/startup-os/blob/master/tools/local_server/LocalServer.java).
* [Docker example](https://github.com/google/startup-os/tree/master/examples/docker): building containers using Bazel (no dockerfile needed!).
* [Kubernetes](https://github.com/google/startup-os/tree/master/examples/k8s): a config file showing how to run a container built with Bazel on k8s.
* [CI example](https://github.com/google/startup-os/tree/master/.circleci): Using CircleCI to run CI (continuous integration) and test all Bazel targets.
* [Firebase](https://firebase.google.com): Java client for storing Protocol Buffers [here](https://github.com/google/startup-os/blob/master/common/firestore/FirestoreClient.java).
* [Dagger](https://github.com/google/dagger): Java Dependency Injection framework  ("Next gen Guice"), see examples [here](https://github.com/google/startup-os/tree/master/examples/dagger).
* [Flogger](https://github.com/google/flogger): Java logger with fluent API, see examples [here](https://github.com/google/startup-os/search?q=com.google.common.flogger.FluentLogger&unscoped_q=com.google.common.flogger.FluentLogger).
* [Android app](https://github.com/google/startup-os/tree/master/examples/android): An Android app built with Bazel and integrated to Firebase.

## Tools
There are several useful tools in the [tools section](https://github.com/google/startup-os/tree/master/tools).

## How to use StartupOS
You can treat StartupOS as a "developer image" with a pre-built setup and associated tools.

You can either:
* Clone this repo and evolve it on your own.
* Treat it as a dependency to your own repo. An example for that is here: https://github.com/oferb/startup-os-example

## Installation
Install [Bazel](https://docs.bazel.build/versions/master/install.html). That's it!

## Build & Test
* Build everything: `./build.sh`
* Run all tests: `./test.sh`

## About monorepos
A monorepo is a software development approach where all code is stored in a single repository.
StartupOS doesn't require you to work with a monorepo, but some things, such as sharing a proto file across front-end and backend, are easier to do in a monorepo.

Some good reads about the monorepo approach:
* [trunkbaseddevelopment.com/monorepos](https://trunkbaseddevelopment.com/monorepos/)
* [Why Google stores billions of lines of code in a single repository](https://cacm.acm.org/magazines/2016/7/204032-why-google-stores-billions-of-lines-of-code-in-a-single-repository/fulltext)

## Platforms
While Bazel supports Linux, Mac and Windows, this repo has only been developed on Linux and Mac, so on Windows, YMMV.

## Contributing
You're welcome to contribute and in doing so, learn these technologies.
You can have a look at the issues list, or at the project [milestones](docs/milestones.md).
