#!/usr/bin/env bash

# Wrapper script for running protoc

platform=$(uname)
pwd
find . -iname protoc-3.6.1-osx-x86_64.zip

if [ "$platform" == "Darwin" ]; then
    ARCHIVE=$(find . -iname protoc-3.6.1-osx-x86_64.zip | head -n1)
elif [ "$platform" == "Linux" ]; then
    ARCHIVE=$(find . -iname protoc-3.6.1-linux-x86_64.zip | head -n1)
else
    echo "protoc does not have a binary for $platform"
    exit 1
fi

if [ ! -f bin/protoc ]; then
    unzip $ARCHIVE bin/protoc >/dev/null
fi

bin/protoc "$@"
