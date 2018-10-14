#!/usr/bin/env bash

# Wrapper script for running buildozer

platform=$(uname)

if [ "$platform" == "Darwin" ]; then
    BUILDOZER_BINARY=$(pwd)/external/buildozer_osx/file/buildozer.osx
elif [ "$platform" == "Linux" ]; then
    BUILDOZER_BINARY=$(pwd)/external/buildozer/file/buildozer
else
    echo "Buildozer does not have a binary for $platform"
    exit 1
fi

cd $BUILD_WORKSPACE_DIRECTORY
$BUILDOZER_BINARY "$@"
