#!/bin/bash

if [[ "$(basename -- "$0")" == "completion.sh" ]]; then
    echo "Don't run $0, source it" >&2
    exit 1
fi

platform=$(uname)
if [ "$platform" == "Darwin" ]; then
	echo "Current OS is macOS, we assume you installed bazel via Homebrew"
    source $(brew --prefix)/etc/bash_completion.d/bazel-complete.bash
elif [ "$platform" == "Linux" ]; then
    echo "Current OS is Linux, we assume you installed bazel via APT"
    source /etc/bash_completion.d/bazel
else
    echo "Unknown platform"
    exit 1
fi
