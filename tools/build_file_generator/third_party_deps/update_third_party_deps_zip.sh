#! /bin/bash

readonly ZIPNAME=third_party_deps.zip

# We need to have all third party dependencies in `bazel-<repo_name>/external` folder
# before creating `third_party_deps.zip`
./build_third_party_deps.sh && ./create_third_party_deps_zip.sh

echo "$ZIPNAME was successfully updated"
