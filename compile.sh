#!/usr/bin/env bash

RED=$(tput setaf 1)
RESET=$(tput sgr0)

# Run with `bazel` command as param (build|test)
if [[ $1 != "build" && $1 != "test" ]]; then
  echo "$RED""Run script with 'build' or 'test' as param$RESET"
  exit 1
fi

bazel $1 //...
exit $?
