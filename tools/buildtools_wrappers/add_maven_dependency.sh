#!/usr/bin/env bash

# Script for adding maven artifact to dependencies.yaml and then regenerating dependencies

for MAVEN_ARTIFACT in "$@"
do
	echo "Adding $MAVEN_ARTIFACT dependency"
	bazel run @bazel_deps//:parse -- add-dep --deps `pwd`/dependencies.yaml --lang java $MAVEN_ARTIFACT
done

bazel run @bazel_deps//:parse -- generate -r `pwd` -s third_party/maven/workspace.bzl -d dependencies.yaml
