#!/usr/bin/env bash

readonly PROTOTXT_FILENAME=third_party_deps.prototxt
readonly ZIP_ARCHIVE_NAME=third_party_deps.zip
readonly REPO_ABS_PATH=$(git rev-parse --show-toplevel)
readonly REPO_NAME=$(basename "$REPO_ABS_PATH")
readonly PROTOTXT_ABS_PATH="$REPO_ABS_PATH/$PROTOTXT_FILENAME"
readonly ZIP_ABS_PATH="$REPO_ABS_PATH/$ZIP_ARCHIVE_NAME"

# get third party folder names (only starts with `mvn`)
external_folder_path="$REPO_ABS_PATH/bazel-$REPO_NAME/external"
declare -a external_folder_content
for entry in "$external_folder_path"/mvn*; do
  current_folder_name=${entry/$external_folder_path\//}
  external_folder_content=("${external_folder_content[@]}" "$current_folder_name")
done

# get third party target names
third_party_folder_path="$REPO_ABS_PATH/third_party"
declare -a third_party_targets
for entry in $(bazel query //third_party/...); do
  third_party_targets=("${third_party_targets[@]}" "$entry")
done

rm -f ${PROTOTXT_ABS_PATH}

for entry in "${external_folder_content[@]}"; do
  jar_content=($(jar -tf "$REPO_ABS_PATH/bazel-$REPO_NAME/external/$entry/jar/$entry.jar"))
  associated_target=''
  for target in "${third_party_targets[@]}"; do
    replaced_target=${target/\/\/third_party\/maven\//mvn}
    replaced_target=${replaced_target//\//_}
    replaced_target=${replaced_target/:/_}
    if [[ ${entry} == ${replaced_target} ]]; then
      associated_target=${target}
      break
    fi
  done

  if [[ "$associated_target" == "" ]]; then
    echo "can't find for: $entry"
  fi

  echo "third_party_dep {" >>${PROTOTXT_ABS_PATH}
  echo "  target: \"$associated_target\"" >>${PROTOTXT_ABS_PATH}

  for class in "${jar_content[@]}"; do
    if [[ ${class} =~ .*.class ]] && [[ ${class} != *'$'* ]]; then
      echo "  java_class: \"$class\"" >>${PROTOTXT_ABS_PATH}
    fi
  done
  echo "}" >>${PROTOTXT_ABS_PATH}
done

rm -f ${ZIP_ABS_PATH}
zip -j ${ZIP_ABS_PATH} ${PROTOTXT_ABS_PATH}
rm -f ${PROTOTXT_ABS_PATH}

echo "$ZIP_ARCHIVE_NAME was successfully created"

exit
