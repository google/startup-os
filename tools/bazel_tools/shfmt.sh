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

# Follow Google's style when formatting scripts
# (-i 2) Indent with 2 spaces
# (-ci) switch cases will be indented
# (-w) write result to file instead of stdout
${BINARY} -i 2 -ci -w $*
