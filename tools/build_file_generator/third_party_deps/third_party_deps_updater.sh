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

# Deleting text between %THIRD_PARTY_DEPS_START% and %THIRD_PARTY_DEPS_END%, while preserving the markers
sed -i.bak '/# %THIRD_PARTY_DEPS_START%/,/# %THIRD_PARTY_DEPS_END%/{//!d;}' ${BUILD_FILE_ABS_PATH} && rm ${BUILD_FILE_ABS_PATH}.bak

for target_to_add in "${actual_third_party_targets[@]}"; do
  target_folder_and_name="${target_to_add##*\/}"
  target_folder="${target_folder_and_name%:*}"
  target_name="${target_folder_and_name##*:}"
  # Removing target name if it has the same name as target folder
  # E.g. "//third_party/maven/com/google/dagger:dagger" >> "//third_party/maven/com/google/dagger"
  if [[ "$target_folder" == "$target_name" ]]; then
    target_to_add=${target_to_add//:${target_name}/}
  fi
  # Adding target_to_add as next line under %START% marker
  sed -i.bak "/# %THIRD_PARTY_DEPS_START%/a \"${target_to_add}\"," ${BUILD_FILE_ABS_PATH} && rm ${BUILD_FILE_ABS_PATH}.bak
done

echo "Third party targets are updated"
