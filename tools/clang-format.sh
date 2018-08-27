#!/usr/bin/env bash

# Script for finding binary for clang-format, and running it

platform=$(uname)
pwd

if [ "$platform" == "Darwin" ]; then
    ARCHIVE=$(find . -iname clang+llvm-6.0.0-x86_64-apple-darwin.tar.xz | head -n1)
elif [ "$platform" == "Linux" ]; then
    ARCHIVE=$(find . -iname clang+llvm-6.0.0-x86_64-linux-gnu-ubuntu-16.04.tar.xz | head -n1)
else
    echo "clang-format does not have a binary for $platform"
    exit 1
fi

if [ ! -f bin/clang-format ]; then
    tar xf $ARCHIVE bin/clang-format >/dev/null
fi

bin/clang-format "$@"

