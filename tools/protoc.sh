#!/usr/bin/env bash

# Wrapper script for running protoc

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
    ARCHIVE=$(find . -iwholename "*protoc_bin_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
    ARCHIVE=$(find . -iwholename "*protoc_bin/file/downloaded" | head -n1)
else
    echo "protoc does not have a binary for $platform"
    exit 1
fi

if [[ ! -f bin/protoc ]]; then
    unzip ${ARCHIVE} bin/protoc >/dev/null
fi

bin/protoc "$@"
