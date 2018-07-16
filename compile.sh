#!/usr/bin/env bash

RED=$(tput setaf 1)
RESET=$(tput sgr0)

# Run with `bazel` command as param (build|test)
if [[ $1 != "build" && $1 != "test" ]]; then
  echo "$RED""Run script with 'build' or 'test' as param$RESET"
  exit 1
fi

# On CircleCI we ignore packages that depend on
# @com_google_protobuf//:protobuf (cpp library),
# which leads to inability to use prebuilt binaries
# and significantly increases build time
if [[ ! -z "$CIRCLECI" ]]; then
  DELETED_PACKAGES="";
else
  DELETED_PACKAGES="--deleted_packages $(cat .circleci/deleted_bazel_packages.txt)";
fi

bazel $1 $DELETED_PACKAGES //...
exit $?
