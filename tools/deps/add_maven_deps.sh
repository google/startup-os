#!/usr/bin/env bash

# Script for adding a new Maven dependency.

# Usage format: tools/deps/add_maven_dep.sh <maven_artifact(s)>
# Example: tools/deps/add_maven_deps.sh junit:junit:4.12
# Example: tools/deps/add_maven_deps.sh junit:junit:4.12 org.json:json:20180130

# Add dependencies to dependencies.yaml:
for MAVEN_ARTIFACT in "$@"; do
  echo "Adding $MAVEN_ARTIFACT dependency"
  bazel run //tools:bazel_deps -- add-dep --deps $(pwd)/dependencies.yaml --lang java $MAVEN_ARTIFACT
done

# Update package-lock.bzl and regenerate third_party/maven dependencies:
bazel run //tools:bazel_deps -- generate -r $(pwd) -s third_party/maven/package-lock.bzl -d dependencies.yaml

# Fix formatting for BUILD files
bazel run //tools/formatter -- --path $(pwd)/third_party --build &>/dev/null
