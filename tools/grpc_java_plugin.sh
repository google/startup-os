#!/usr/bin/env bash

# Wrapper script for running grpc_java_plugin

platform=$(uname)

if [[ "$platform" == "Darwin" ]]; then
    PLUGIN_BINARY=$(find . -iwholename "*grpc_java_plugin_osx/file/downloaded" | head -n1)
elif [[ "$platform" == "Linux" ]]; then
    PLUGIN_BINARY=$(find . -iwholename "*grpc_java_plugin_linux/file/downloaded" | head -n1)
else
    echo "grpc_java_plugin does not have a binary for $platform"
    exit 1
fi

${PLUGIN_BINARY} "$@"
