#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from cloned repo)
#
# bazel build //tools/aa:aa_tool_deploy.jar
# echo "export AA_BASE=$(pwd)" >> ~/.bashrc
# echo "source $(pwd)/tools/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile


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
