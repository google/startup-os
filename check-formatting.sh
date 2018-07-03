#!/usr/bin/env bash

# Run it before committing to verify whether all files
# are properly formatted so you won't fail early on review
# Execute from repo root or, if using aa from base/head/startup-os

RED=$(tput setaf 1)
RESET=$(tput sgr0)

npm install &>/dev/null

PROTO_OPTIONS="--proto_compiler //external:proto_compiler --proto_toolchain_for_java //external:proto_java_toolchain"
BUILD_FILES=$(find `pwd` -type f \( -name BUILD.bazel -or -name BUILD \) | grep -v node_modules)
# when ran with -mode=check buildifier exits with non-zero code
# if any BUILD files need formatting
bazel run $PROTO_OPTIONS //tools:buildifier -- -mode=check $BUILD_FILES &>/dev/null || (
	echo "$RED[!] BUILD files are not formatted$RESET";
	echo "Please run ''./fix-formatting.sh'' to fix it"
	exit 1
)

bazel run $PROTO_OPTIONS //tools/simple_formatter -- --path $(pwd) --java --python --proto --cpp --ignore_directories $(pwd)/node_modules/,$(pwd)/tools/local_server/web_login/node_modules/ &>/dev/null
if [[ ! -z $(git status -s) ]]; then
	echo "$RED[!] Source files are not formatted$RESET";
	echo "Please run ''./fix-formatting.sh'' to fix it"
	exit 1
fi
