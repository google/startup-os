#!/usr/bin/env bash

# Wrapper script for running grpc_java_plugin

platform=$(uname)

if [ "$platform" == "Darwin" ]; then
    PLUGIN_BINARY=$(pwd)/bazel-out/host/bin/tools/grpc_java_plugin.runfiles/grpc_java_plugin_osx/file/grpc_java_plugin_osx
elif [ "$platform" == "Linux" ]; then
    PLUGIN_BINARY=$(pwd)/bazel-out/host/bin/tools/grpc_java_plugin.runfiles/grpc_java_plugin_linux/file/grpc_java_plugin_linux
else
    echo "grpc_java_plugin does not have a binary for $platform"
    exit 1
fi
$PLUGIN_BINARY "$@"
