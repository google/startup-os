#!/usr/bin/env bash

AA_BINARY="java -jar $AA_BASE/bazel-bin/tools/aa/aa_tool_deploy.jar"

function aa {
    if [ "$1" = "workspace" ]; then
        AA_RESULT=$(eval $AA_BINARY $*)
        $AA_RESULT
    else
        eval $AA_BINARY $*
    fi
}

export -f aa