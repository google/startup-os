#!/usr/bin/env bash

# Wrapper script for running shfmt

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
  BINARY=$(find . -iwholename "*shfmt_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
  BINARY=$(find . -iwholename "*shfmt/file/downloaded" | head -n1)
else
  echo "shfmt does not have a binary for $platform"
  exit 1
fi

${BINARY} -i 2 -ci -w $*
