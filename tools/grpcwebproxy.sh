#!/usr/bin/env bash
 # Wrapper script for running grpcwebproxy
 platform=$(uname)
 if [ "$platform" == "Darwin" ]; then
    BINARY=$(pwd)/bazel-out/host/bin/tools/grpcwebproxy.runfiles/grpcwebproxy_osx/file/grpcwebproxy_osx
elif [ "$platform" == "Linux" ]; then
    BINARY=$(pwd)/bazel-out/host/bin/tools/grpcwebproxy.runfiles/grpcwebproxy_linux/file/grpcwebproxy_linux
else
    echo "grpcwebproxy does not have a binary for $platform"
    exit 1
fi
 $BINARY "$@"
