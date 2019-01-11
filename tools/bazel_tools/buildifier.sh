#!/usr/bin/env bash

# Wrapper script for running buildifier

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
  BINARY=$(find . -iwholename "*buildifier_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
  BINARY=$(find . -iwholename "*buildifier/file/downloaded" | head -n1)
else
  echo "Buildifier does not have a binary for $platform"
  exit 1
fi

${BINARY} $*
