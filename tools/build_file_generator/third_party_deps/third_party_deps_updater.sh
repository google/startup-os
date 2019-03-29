#!/usr/bin/env bash

# A script for updating third party dependencies inside third_party_deps_tool java_binary

readonly REPO_ABS_PATH=$(git rev-parse --show-toplevel)
# TODO: Make the getting of the path independent of the project
readonly BUILD_FILE_ABS_PATH="$REPO_ABS_PATH/tools/build_file_generator/third_party_deps/BUILD"

# Getting public third-party target names
actual_third_party_targets=()
for entry in $(bazel query 'attr("visibility", "//visibility:public", //third_party/...)'); do
  actual_third_party_targets+=("$entry")
done

# Deleting text between %START% and %END%, while preserving the markers
sed -i.bak '/# %START%/,/# %END%/{//!d;}' ${BUILD_FILE_ABS_PATH} && rm ${BUILD_FILE_ABS_PATH}.bak

for target_to_add in "${actual_third_party_targets[@]}"; do
  # Adding target_to_add as next line under %START% marker
  sed -i.bak "/# %START%/a \"${target_to_add}\"," ${BUILD_FILE_ABS_PATH} && rm ${BUILD_FILE_ABS_PATH}.bak
done

echo "Third party targets are updated"
