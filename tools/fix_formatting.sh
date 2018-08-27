#!/usr/bin/env bash

# Run it before committing to verify whether all files
# are properly formatted so you won't fail early on review
# Execute from repo root or, if using aa from base/head/startup-os

RED=$(tput setaf 1)
RESET=$(tput sgr0)

bazel run //tools:bazel_deps -- format-deps \
  --overwrite \
  --deps $(pwd)/dependencies.yaml

bazel run //tools/formatter -- \
  --path $(pwd) \
  --java --python --proto --cpp --build \
  --ignore_directories $(find $(pwd) -name node_modules -type d | paste -s -d , -) \
  &>/dev/null

# Prints out an error only if both conditions are satisfied:
# * we are on CircleCI
# * working tree contains unstaged changes
# When ran locally it silently fixes everything.
if [[ ! -z "$CIRCLECI" && ! -z $(git status -s) ]]; then
	echo "$RED[!] Source files are not formatted$RESET";
	echo "Please run ''$0'' to fix it"
	exit 1
fi
