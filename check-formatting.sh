#!/usr/bin/env bash

# Pre-commit hook for StartupOS
# Run it before committing to verify whether all files 
# are properly formatted so you won't fail early on review
# Either do it manually or by creating a symlink
# To do this, execute, from repo root
# ln -s $(pwd)/pre-commit.sh $(pwd)/.git/hooks/pre-commit


npm install &>/dev/null

BUILD_FILES=$(find `pwd` -type f \( -name BUILD.bazel -or -name BUILD \) | grep -v node_modules)

bazel run //tools:buildifier -- -mode=check $BUILD_FILES &>/dev/null || (
	echo "$(tput setaf 1)[!] BUILD files are not formatted$(tput sgr0)";
	echo "Please run ''yarn buildifier'' to fix it" 
	exit 1
)

bazel run @simple_formatter//:simple_formatter_tool -- --path $(pwd) --java --python --proto --cpp --ignore_directories $(pwd)/node_modules/,$(pwd)/tools/local_server/web_login/node_modules/ &>/dev/null
if [[ ! -z $(git status -s) ]]; then
	echo "$(tput setaf 1)[!] Source files are not formatted$(tput sgr0)";
	echo "Please run ''bazel run //tools/simple_formatter:simple_formatter -- --path \$(pwd) --java --python --proto --cpp'' to fix it" 
	exit 1
fi
