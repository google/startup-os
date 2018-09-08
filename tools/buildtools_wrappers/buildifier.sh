#!/usr/bin/env bash

# Wrapper script for running buildifier

platform=$(uname)

if [ "$platform" == "Darwin" ]; then
    BINARY=$(pwd)/external/buildifier_osx/file/buildifier.osx
elif [ "$platform" == "Linux" ]; then
    BINARY=$(pwd)/external/buildifier/file/buildifier
else
    echo "Buildifier does not have a binary for $platform"
    exit 1
fi

$BINARY $*
