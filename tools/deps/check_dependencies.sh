#!/usr/bin/env bash

# This tool updates dependencies at third_party/maven with dependencies.yaml.
# If run from CircleCI, it prints out an error if dependencies are not synced.

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)

git diff --name-only origin/master | grep --quiet dependencies.yaml
if [ $? -eq 0 ]; then
  echo "$RED[!] dependencies.yaml was modified, running the check$RESET"
else
  echo "$GREEN[!] dependencies.yaml was not modified, exiting (code $?) $RESET"
  exit 0
fi

# Regenerate dependencies
OUTPUT=$(
  bazel run //tools:bazel_deps -- generate \
    -r $(pwd) \
    -s third_party/maven/package-lock.bzl \
    -d dependencies.yaml 2>&1
)
if [ $? -ne 0 ]; then
  echo "$RED[!] bazel_deps failed. Output:"
  echo $OUTPUT
  exit 1
fi

# Format generated BUILD files
OUTPUT=$(bazel run //tools/formatter -- --path $(pwd)/third_party/maven/ --build 2>&1)
if [ $? -ne 0 ]; then
  echo "$RED[!] formatter failed. Output:"
  echo $OUTPUT
  exit 1
fi

# Print error if on CircleCI and dependencies were not up-to-date
if [[ ! -z "$CIRCLECI" && ! -z $(git status -s) ]]; then
  echo "$RED[!] Dependency tree does not match dependencies.yaml$RESET"
  git status
  git --no-pager diff
  echo "Please run ''$0'' to fix it"
  exit 1
fi
