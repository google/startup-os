#!/usr/bin/env bash

# Wrapper script for running unused_deps

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
    BINARY=$(find . -iwholename "*unused_deps_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
    BINARY=find . -iwholename "*unused_deps/file/downloaded" | head -n1
else
    echo "unused_deps does not have a binary for $platform"
    exit 1
fi

${BINARY} $*
