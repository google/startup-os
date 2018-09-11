#!/usr/bin/env bash

# Wrapper script for running buildifier

platform=$(uname)

if [ "$platform" == "Darwin" ]; then
    BINARY=$JAVA_RUNFILES/buildifier_osx/file/buildifier.osx
elif [ "$platform" == "Linux" ]; then
    BINARY=$JAVA_RUNFILES/buildifier/file/buildifier
else
    echo "Buildifier does not have a binary for $platform"
    exit 1
fi

$BINARY $*
