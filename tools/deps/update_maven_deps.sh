#!/usr/bin/env bash

# Script for updating Maven deps after modifying or deleting a dependency in dependencies.yaml.
# Usage: tools/deps/update_maven_deps.sh

bazel run //tools:bazel_deps -- generate -r `pwd` -s third_party/maven/package-lock.bzl -d dependencies.yaml

# Fix formatting for BUILD files
bazel run //tools/formatter -- --path $(pwd)/third_party --build &>/dev/null
