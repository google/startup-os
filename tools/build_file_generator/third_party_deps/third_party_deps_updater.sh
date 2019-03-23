#!/usr/bin/env bash

# A script for updating third party dependencies inside third_party_deps_tool java_binary

readonly REPO_ABS_PATH=$(git rev-parse --show-toplevel)
readonly REPO_NAME=$(basename "$REPO_ABS_PATH")
readonly JAVA_BINARY_NAME="third_party_deps_tool"
# TODO: Make the getting of the path independent of the project
readonly BUILD_FILE_ABS_PATH="$REPO_ABS_PATH/tools/build_file_generator/third_party_deps/BUILD"

# Getting public third-party target names
third_party_folder_path="$REPO_ABS_PATH/third_party"
declare -a actual_third_party_targets
for entry in $(bazel query 'attr("visibility", "//visibility:public", //third_party/...)'); do
  actual_third_party_targets=("${actual_third_party_targets[@]}" "$entry")
done

# Returns true if passed line contains `"third_party_deps_tool",`. We assume it's `third_party_deps_tool` java_binary rule.
is_third_party_deps_tool_rule() {
  line="$1"
  if [[ "$line" == *"\"third_party_deps_tool\","* ]]; then
    inside_third_party_deps_tool_rule="true"
  else
    inside_third_party_deps_tool_rule="false"
  fi
  echo "$inside_third_party_deps_tool_rule"
}

# Getting third-party dependencies already described in `third_party_deps_tool` java_binary rule.
declare -a already_exists_third_party_deps
counter=0
inside_third_party_deps_tool_rule="false"
while IFS= read -r line; do
  if [[ ("$inside_third_party_deps_tool_rule" == "false") ]]; then
    inside_third_party_deps_tool_rule=$(is_third_party_deps_tool_rule "$line")
  fi
  # `)` sign means the end of the rule block
  if [[ ("$inside_third_party_deps_tool_rule" == "true") && ("$line" == *")"*) ]]; then
    inside_third_party_deps_tool_rule="false"
  fi
  if [[ ("$inside_third_party_deps_tool_rule" == "true") && ("$line" == *"//third_party/maven/"*) ]]; then
    replaced_target=${line//[[:space:]]/}
    replaced_target=${replaced_target/\"/}
    replaced_target=${replaced_target/\",/}
    already_exists_third_party_deps=("${already_exists_third_party_deps[@]}" "$replaced_target")
  fi
done <"$BUILD_FILE_ABS_PATH"

# Converts short target name to full name. E.g. `//third_party/maven/junit` to `//third_party/maven/junit:junit`
get_full_target_name() {
  short_target_name="$1"
  target_name="${short_target_name##*\/}"
  full_target_name="$short_target_name:$target_name"
  echo "$full_target_name"
}

# Getting targets to add
declare -a targets_to_add
for actual_target in "${actual_third_party_targets[@]}"; do
  do_nothing=false
  for already_exists_dep in "${already_exists_third_party_deps[@]}"; do
    if [[ ${already_exists_dep} != *":"* ]]; then
      full_target_name=$(get_full_target_name "${already_exists_dep}")
    else
      full_target_name=${already_exists_dep}
    fi
    if [[ "$actual_target" == "$full_target_name" ]]; then
      do_nothing=true
      break
    fi
  done
  if [[ "$do_nothing" == "false" ]]; then
    targets_to_add=("${targets_to_add[@]}" "$actual_target")
  fi
done

# Getting targets to remove
declare -a targets_to_remove
for already_exists_dep in "${already_exists_third_party_deps[@]}"; do
  if [[ ${already_exists_dep} != *":"* ]]; then
    full_target_name=$(get_full_target_name "${already_exists_dep}")
  else
    full_target_name=${already_exists_dep}
  fi
  do_nothing=false
  for actual_target in "${actual_third_party_targets[@]}"; do
    if [[ "$actual_target" == "$full_target_name" ]]; then
      do_nothing=true
      break
    fi
  done
  if [[ "$do_nothing" == "false" ]]; then
    targets_to_remove=("${targets_to_remove[@]}" "$already_exists_dep")
  fi
done

if [[ "${#targets_to_add[@]}" > 0 ]]; then
  echo "Targets to add: ${#targets_to_add[@]}"
  for target_to_add in "${targets_to_add[@]}"; do
    echo "${target_to_add}"
  done

  # Returns line number of the first dependency in third_party_deps_tool java_binary
  get_third_party_deps_tool_start_deps_line_number() {
    is_third_party_deps_tool_rule=false
    counter=0
    while IFS= read -r line; do
      counter=$((counter + 1))
      if [[ "$line" == *"\"third_party_deps_tool\","* ]]; then
        is_third_party_deps_tool_rule=true
      fi
      if [[ ("$is_third_party_deps_tool_rule" == true) && ("$line" == *"deps = ["*) ]]; then
        return $((counter + 1))
      fi
    done <"$BUILD_FILE_ABS_PATH"
  }
  echo "Adding new targets..."
  echo ${#targets_to_add[@]}
  readonly TEMP_BUILD_FILE_ABS_PATH="$BUILD_FILE_ABS_PATH.txt.tmp"
  if [[ -f "$TEMP_BUILD_FILE_ABS_PATH" ]]; then
    rm ${TEMP_BUILD_FILE_ABS_PATH}
  fi
  get_third_party_deps_tool_start_deps_line_number
  line_to_add_dep=$?
  for target_to_add in "${targets_to_add[@]}"; do
    if [[ -f "$TEMP_BUILD_FILE_ABS_PATH" ]]; then
      cp ${TEMP_BUILD_FILE_ABS_PATH} ${BUILD_FILE_ABS_PATH}
      sed "${line_to_add_dep}i\"${target_to_add}\"," ${BUILD_FILE_ABS_PATH} >${TEMP_BUILD_FILE_ABS_PATH}
    else
      sed "${line_to_add_dep}i\"${target_to_add}\"," ${BUILD_FILE_ABS_PATH} >${TEMP_BUILD_FILE_ABS_PATH}
    fi
    echo "${target_to_add} is added"
  done
  cp ${TEMP_BUILD_FILE_ABS_PATH} ${BUILD_FILE_ABS_PATH}
  rm ${TEMP_BUILD_FILE_ABS_PATH}

else
  echo "There are no targets to add"
fi

if [[ "${#targets_to_remove[@]}" > 0 ]]; then
  echo "Targets to remove: ${#targets_to_remove[@]}"
  for target_to_remove in "${targets_to_remove[@]}"; do
    echo "${target_to_remove}"
  done

  # Finds and returns line number of given target in third_party deps tool java_binary deps
  get_line_number_of_target() {
    line_number=0
    target="$1"
    inside_third_party_deps_tool_rule="false"
    while IFS= read -r line; do
      line_number=$((line_number + 1))

      if [[ ("$inside_third_party_deps_tool_rule" == "false") ]]; then
        inside_third_party_deps_tool_rule=$(is_third_party_deps_tool_rule "$line")
      fi

      # `)` sign means the end of the rule block
      if [[ ("$inside_third_party_deps_tool_rule" == "true") && ("$line" == *")"*) ]]; then
        inside_third_party_deps_tool_rule="false"
      fi

      if [[ ("$inside_third_party_deps_tool_rule" == "true") ]]; then

        if [[ "$line" == *"$target"* ]]; then
          return "$line_number"
        fi
      fi
    done <"$BUILD_FILE_ABS_PATH"
  }

  echo "Removing targets..."
  for target_to_remove in "${targets_to_remove[@]}"; do
    get_line_number_of_target "$target_to_remove"
    line_number_to_remove=$?
    if [[ ${line_number_to_remove} > 0 ]]; then
      sed -i "$line_number_to_remove"d ${BUILD_FILE_ABS_PATH}
      echo "${target_to_remove} is removed"
    fi
  done
else
  echo "There are no targets to remove"
fi
