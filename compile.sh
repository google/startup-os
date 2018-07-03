#!/usr/bin/env bash

# Builds all targets and runs all tests
if [[ ! -z "$CIRCLECI" ]]; then
  DELETED_PACKAGES="";
else
  DELETED_PACKAGES="--deleted_packages $(cat .circleci/deleted_bazel_packages.txt)";
fi

PROTO_OPTIONS="--proto_compiler //external:proto_compiler --proto_toolchain_for_java //external:proto_java_toolchain"

time bazel test $PROTO_OPTIONS $DELETED_PACKAGES //...
exit $?
