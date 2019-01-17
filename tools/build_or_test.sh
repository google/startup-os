#!/usr/bin/env bash

# Build or test all targets.
# Note that 'testing all targets' also builds all buildable targets.
# If ANDROID_HOME is not set, skips Android targets.

# Usage: tools/build_or_test.sh (build|test)

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)

CIRCLECI_MAX_ATTEMPTS=10

function run_bazel() {
  if hash bazel 2>/dev/null; then
    # bazel is available in PATH
    bazel "$@"
  else
    # bazelisk is used to download it
    ./bazelisk "$@"
  fi
}

export -f run_bazel

function bazel_build() {
  if [[ -z "$ANDROID_HOME" ]]; then
    # Ignore third_party, node_modules and android targets
    run_bazel $1 $(run_bazel query '//... except //third_party/... except filter(node_modules, //...) except kind("android_.* rule", //...)')
    return $?
  else
    # Ignore just third_party and node_modules
    run_bazel $1 $(run_bazel query '//... except //third_party/... except filter(node_modules, //...)')
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

if [[ ! -z "$CIRCLECI" ]]; then
  echo "$RED""Due to flakiness in bazel execution on CircleCI, trying to build several times"
  for i in $(seq 1 ${CIRCLECI_MAX_ATTEMPTS}); do
    echo "$RED""[Attempt $i/${CIRCLECI_MAX_ATTEMPTS}]: building$RESET"
    bazel_build $1
    if [[ $? -eq 0 ]]; then
      echo "$GREEN""[Attempt $i/${CIRCLECI_MAX_ATTEMPTS}]: successful$RESET"
      exit 0
    fi
  done

  echo "$RED""[Attempts exhausted]: Seems it's a problem with your code and not a CircleCI flake.$RESET"
  exit 1
fi

bazel_build $1
exit $?
