#!/usr/bin/env bash

# A script for updating third party dependencies inside third_party_deps_tool java_binary

readonly REPO_ABS_PATH=$(git rev-parse --show-toplevel)
# TODO: Make the getting of the path independent of the project
readonly BUILD_FILE_ABS_PATH="$REPO_ABS_PATH/tools/build_file_generator/third_party_deps/BUILD"

# Getting public third-party target names
third_party_folder_path="$REPO_ABS_PATH/third_party"
actual_third_party_targets=()
for entry in $(bazel query 'attr("visibility", "//visibility:public", //third_party/...)'); do
  actual_third_party_targets+=("$entry")
done

# Deleting old targets
sed -i '/# %START%/,/# %END%/{//!d;}' ${BUILD_FILE_ABS_PATH}

# Adding new targets
for target_to_add in "${actual_third_party_targets[@]}"; do
  sed -i "/# %START%/a \"${target_to_add}\"," ${BUILD_FILE_ABS_PATH}
done

echo "Third party targets are updated"
