#!/usr/bin/env bash

RED=$(tput setaf 1)
RESET=$(tput sgr0)

# Run with `bazel` command as param (build|test)
if [[ $1 != "build" && $1 != "test" ]]; then
  echo "$RED""Run script with 'build' or 'test' as param$RESET"
  exit 1
fi

if [ -z "$ANDROID_HOME" ]; then
  echo "$RED""Set ANDROID_HOME variable to valid Android SDK location$RESET"
  echo "$RED""Run ./tools/get-android-sdk.sh to download it$RESET"
  exit 2
fi

bazel $1 $DELETED_PACKAGES //...
exit $?
