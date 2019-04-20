#!/usr/bin/env bash

# Wrapper script for running tsfmt

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
  BINARY=$(find . -name "cli-macos" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
  BINARY=$(find . -name "cli-linux" | head -n1)
else
  echo "tsfmt does not have a binary for $platform"
  exit 1
fi

${BINARY} -r $*
