#!/usr/bin/env bash

# Script for adding maven artifact to dependencies.yaml and then regenerating dependencies
# Usage format: sh add_maven_dependency.sh <maven_artifact(s)>
# <maven_artifact> i.e com.scireum:sirius-kernel:8.0.1
# Usage example: sh add_maven_dependency.sh com.scireum:sirius-kernel:8.0.1

for MAVEN_ARTIFACT in "$@"
do
  echo "Adding $MAVEN_ARTIFACT dependency"
  bazel run @bazel_deps//:parse -- add-dep --deps `pwd`/dependencies.yaml --lang java $MAVEN_ARTIFACT
done

# Regenerate dependencies
bazel run @bazel_deps//:parse -- generate -r `pwd` -s third_party/maven/workspace.bzl -d dependencies.yaml
