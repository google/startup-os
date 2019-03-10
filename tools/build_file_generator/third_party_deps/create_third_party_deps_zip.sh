#!/usr/bin/env bash

readonly FILENAME=third_party_deps.prototxt
readonly ZIPNAME=third_party_deps.zip
readonly repo_path=$(git rev-parse --show-toplevel)
readonly repo_name=$(basename "$repo_path")

# get third party folder names (only starts with `mvn`)
external_folder_path="$repo_path/bazel-$repo_name/external"
declare -a external_folder_content
for entry in "$external_folder_path"/mvn*; do
  current_folder_name=${entry/$external_folder_path\//}
  external_folder_content=("${external_folder_content[@]}" "$current_folder_name")
done

# get third party target names
third_party_folder_path="$repo_path/third_party"
declare -a third_party_targets
for entry in $(bazel query //third_party/...); do
  third_party_targets=("${third_party_targets[@]}" "$entry")
done

rm -f $FILENAME

for entry in "${external_folder_content[@]}"; do
  jar_content=($(jar -tf "$repo_path/bazel-$repo_name/external/$entry/jar/$entry.jar"))
  associated_target=''
  for target in "${third_party_targets[@]}"; do
    replaced_target=${target/\/\/third_party\/maven\//mvn}
    replaced_target=${replaced_target//\//_}
    replaced_target=${replaced_target/:/_}
    if [ $entry == $replaced_target ]; then
      associated_target=$target
      break
    fi
  done

  if [ "$associated_target" == "" ]; then
    echo "can't find for: $entry"
  fi

  echo "third_party_dep {" >>$FILENAME
  echo "  target: \"$associated_target\"" >>$FILENAME

  for class in "${jar_content[@]}"; do
    if [[ $class =~ .*.class ]] && [[ $class != *'$'* ]]; then
      echo "  java_class: \"$class\"" >>$FILENAME
    fi
  done
  echo "}" >>$FILENAME
done

rm -f $ZIPNAME
zip $ZIPNAME $FILENAME
rm -f $FILENAME

echo "$ZIPNAME was successfully created"

exit
