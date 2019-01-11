#!/usr/bin/env bash

# Wrapper script for running buildozer

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
  BINARY=$(find . -iwholename "*buildozer_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
  BINARY=$(find . -iwholename "*buildozer/file/downloaded" | head -n1)
else
  echo "Buildozer does not have a binary for $platform"
  exit 1
fi

${BINARY} $*
