# StartupOS

[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/google/startup-os)
[![CircleCI](https://circleci.com/gh/google/startup-os/tree/master.svg?style=svg)](https://circleci.com/gh/google/startup-os/tree/master)

> Working examples for using Google's Open Source tools and deploying to the cloud.

### StartupOS includes
* Google's open source libraries and tools, integrated together
* Easy Cloud setup and deployment
* Ready-to-run examples of Android and iOS native apps and Angular web apps.

### How can StartupOS help me?
Compared to example repos (e.g https://github.com/googlesamples), which typically have a very specific example, usually for one tool or technology, this repo shows how to integrate multiple technologies together. Often, when trying to use several tools, you run into dependency issues, integration issues, or just wonder what's the best practice for working with them together.

StartupOS can help you with working examples and best-practices that "just work" across multiple tools and technologies.

### How to use StartupOS
You can:
* Clone this repo, and then adapt it to your needs.
* Use it as a reference and copy-paste parts of it to your own repo.
* Use parts of it from your own repo. For an example of that, see https://github.com/hasadna/hasadna (look for `@startup_os` in BUILD files)

### Installation
Install [Bazel](https://docs.bazel.build/versions/master/install.html).
If you already have Android SDK (i.e. you installed Android Studio),
set environment variable `export ANDROID_HOME=<…>` (
for macOS: `$HOME/Library/Android/sdk/`). Otherwise, start from executing
`./get-android-sdk.sh`

Now you can build any target you want.
To build all targets:
  `./compile.sh build`

To run all tests:
`./compile.sh test`

### Milestones
#### ✓ Milestone I
Working examples of:
* ✓ [Bazel](https://bazel.build)
* ✓ [Protocol Buffers](https://developers.google.com/protocol-buffers): See [examples](https://github.com/search?utf8=%E2%9C%93&q=repo%3Agoogle%2Fstartup-os+extension%3Aproto&type=Code&ref=advsearch&l=&l=).
* ✓ [gRPC](https://grpc.io): Example [server](https://github.com/google/startup-os/blob/master/tools/local_server/LocalServer.java).
* ✓ [Dagger](https://github.com/google/dagger): See [examples](https://github.com/google/startup-os/tree/master/examples/dagger).
* ✓ [Flogger](https://github.com/google/flogger): See [examples](https://github.com/google/startup-os/search?q=com.google.common.flogger.FluentLogger&unscoped_q=com.google.common.flogger.FluentLogger).
* ✓ [Firebase](https://firebase.google.com) integration with Protocol Buffers, e.g [Java client](https://github.com/google/startup-os/blob/master/common/firestore/FirestoreClient.java) for REST API.

#### Milestone II
Working examples of:
* ✓ Bazel building Docker containers: See [here](https://github.com/google/startup-os/tree/master/examples/docker)
* ✓ Continuous Integration: Using CircleCI, see [config](https://github.com/google/startup-os/tree/master/.circleci).
* ✓ [Error Prone](https://github.com/google/error-prone): Error Prone is actually already [integrated](https://blog.bazel.build/2015/06/25/ErrorProne.html) into Bazel.
* ✓ [Google Java Formatter](https://github.com/google/google-java-format): Integrated into the CI.
* [ABC](http://g.co/ng/abc) (Angular Bazel Closure)
* [j2objc](https://developers.google.com/j2objc)
* [Kubernetes](https://kubernetes.io)
* [Dagger on Android](https://google.github.io/dagger/android.html)
* More goodies...

## Other repos
Here are some other repos with examples of Bazel, gRPC and other related technologies:
* Go, C++, Java: https://github.com/lucperkins/colossus
* Go: https://github.com/Staffjoy/v2
* Java and also some C++ and Python: https://github.com/apache/incubator-heron

## Feedback
If you're interested in StartupOS, please let us know!
You can fill in this form: https://goo.gl/forms/jfAH0wLgedE8GoWg2
