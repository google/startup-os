#!/usr/bin/env bash

# This tool updates dependencies at third_party/maven with dependencies.yaml.
# If run from CircleCI, it prints out an error if dependencies are not synced.

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)

BAZEL_DEPS_CACHE="$HOME/bazel_deps_cache"
DEP_CHECKSUM=($(sha1sum $(pwd)/dependencies.yaml))
CACHED_RESULT="$BAZEL_DEPS_CACHE/$DEP_CHECKSUM"

if [[ ! -z "$CIRCLECI" ]]; then
  mkdir -p $BAZEL_DEPS_CACHE
  isok=$(<$CACHED_RESULT)

  if [ "$isok" == "OK" ]; then
    echo "$GREEN[CACHE] Dependency tree matches dependencies.yaml$RESET"
    exit 0
  elif [ -f $CACHED_RESULT ]; then
    echo "$RED[CACHE] Cache file is found but does not match format$RESET"
    echo "This should not have happened. Re-run without caching"
    exit 1
  fi
fi

# Regenerate dependencies
bazel run @bazel_deps//:parse -- generate \
  -r $(pwd) \
  -s third_party/maven/workspace.bzl \
  -d dependencies.yaml \
  &>/dev/null

# Format generated BUILD files
bazel run //tools/formatter -- \
  --path $(pwd)/third_party/maven/ \
  --build \
  &>/dev/null

# Print error if on CircleCI
if [[ ! -z "$CIRCLECI" && ! -z $(git status -s) ]]; then
  echo "$RED[!] Dependency tree does not match dependencies.yaml$RESET"
  echo "Please run ''$0'' to fix it"
  exit 1
elif [[ ! -z "$CIRCLECI" ]]; then
  echo "OK" > "$CACHED_RESULT"
fi
