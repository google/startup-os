#!/usr/bin/env bash

# Build or test all targets.
# Note that 'testing all targets' also builds all buildable targets.
# If ANDROID_HOME is not set, skips Android targets.

# Usage: tools/build_or_test.sh (build|test)

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)

function bazel_build() {
  if [[ -z "$ANDROID_HOME" ]]; then
    # Ignore third_party, node_modules and android targets
    bazel query '//... except //third_party/... except filter(node_modules, //...) except kind("android_.* rule", //...)' | xargs bazel $1
    return $?
  else
    # Ignore just third_party and node_modules
    bazel query '//... except //third_party/... except filter(node_modules, //...)' | xargs bazel $1
    return $?
  fi
}

# Warn if ANDROID_HOME is not set.
if [[ -z "$ANDROID_HOME" ]]; then
  echo "$RED""ANDROID_HOME not set, skipping Android targets. See examples/android for more details.$RESET"
fi

# Check we have (build|test) param
if [[ $1 != "build" && $1 != "test" ]]; then
  echo "$RED""Run script with 'build' or 'test' as param$RESET"
  exit 1
fi

bazel_build $1
exit $?
