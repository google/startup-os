#!/usr/bin/env bash

# Wrapper script for running clang_format

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
  BINARY=$(find . -iwholename "*clang_format_bin_osx/file/downloaded" | head -n1)
  echo "BINARY IS $BINARY"
elif [[ "$platform" == "Linux" ]]; then
  BINARY=$(find . -iwholename "*clang_format_bin/file/downloaded" | head -n1)
else
  echo "clang_format does not have a binary for $platform"
  exit 1
fi

${BINARY} $*
