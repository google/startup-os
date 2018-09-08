#!/usr/bin/env bash

# Wrapper script for running clang-format

platform=$(uname)

if [ "$platform" == "Darwin" ]; then
    BINARY=$(pwd)/external/clang_format_bin/file/clang_format_bin_osx
elif [ "$platform" == "Linux" ]; then
    BINARY=$(pwd)/external/clang_format_bin/file/clang_format_bin
else
    echo "clang-format does not have a binary for $platform"
    exit 1
fi

$BINARY "$@"
