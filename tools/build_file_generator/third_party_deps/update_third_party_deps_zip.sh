#! /bin/bash

readonly repo_path=$(git rev-parse --show-toplevel)
readonly build_command="$repo_path/tools/build_or_test.sh build"
readonly ZIPNAME=third_party_deps.zip

# We need to build whole project to have all dependencies in `bazel-<repo_name>/external` folder
# before creating `third_party_deps.zip`
$build_command && ./create_third_party_deps_zip.sh

echo "$ZIPNAME was successfully updated"
