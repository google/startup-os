#!/usr/bin/env bash

AA_BINARY="java -jar $AA_BASE/bazel-bin/tools/aa/aa_tool_deploy.jar"


function aa {
    if [ "$1" = "workspace" ]; then
        # for workspace command instead of letting `aa` print to stdout
        # we need to capture its output and execute it as command
        AA_RESULT=$(eval $AA_BINARY $*)
        $AA_RESULT
    else
        # if command is not workspace, let `aa` execute as is
        eval $AA_BINARY $*
    fi
}

# make aa available as command
export -f aa
