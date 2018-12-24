#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from base/head repo)
# echo "source `pwd`/startup-os/tools/reviewer/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile

# Debugging:
# To compile aa from a workspace: 'export AA_STARTUP_OS_REPO_OVERRIDE=<>'
# To undo: 'unset AA_STARTUP_OS_REPO_OVERRIDE'

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)

function set_STARTUP_OS_REPO() {
    local debug=$1
    set_AA_BASE
    if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1

    fi
    if [[ -z "$AA_STARTUP_OS_REPO_OVERRIDE" ]]; then
        # we want to use `aa` from head
        export STARTUP_OS_REPO="$AA_BASE/head/startup-os/"
    else
        # there's a workspace we want to use `aa` from
        export STARTUP_OS_REPO="$AA_BASE/ws/$AA_STARTUP_OS_REPO_OVERRIDE/startup-os/"

        if [[ "${debug}" -eq 1 ]]; then
          echo "$RED[DEBUG]: using aa from ws $AA_BASE/ws/$AA_STARTUP_OS_REPO_OVERRIDE/startup-os/$RESET"
        fi
    fi
}

function bazel_build() {
    local target=$1
    local force_compile=$2

    #sed replaces bazel target name with name of the binary 'bazel build' would produce
    #by doing the following (sed commands are split by ;):
    #
    #add bazel-bin/ to the beginning
    #replace // with nothing
    #replace : with /
    local binary_for_target=$(echo "$target" | sed -e 's|^|bazel-bin/|g; s|//||g; s|:|/|g;')

    set_STARTUP_OS_REPO
    pushd $STARTUP_OS_REPO &> /dev/null

    if [[ ! -f ${binary_for_target} ]] || [[ "${force_compile}" -eq 1 ]]; then
        bazel build ${target} &> /dev/null
        return_code="$?"
    fi

    popd &> /dev/null
    return ${return_code}
}

function bazel_run() {
    local target=$1
    shift;
    local force_compile=$1
    shift;
    local args="$@"

    #sed replaces bazel target name with name of the binary 'bazel build' would produce
    #by doing the following (sed commands are split by ;):
    #
    #add bazel-bin/ to the beginning
    #replace // with nothing
    #replace : with /
    local binary_for_target=$(echo "$target" | sed -e 's|^|bazel-bin/|g; s|//||g; s|:|/|g;')

    set_STARTUP_OS_REPO
    pushd $STARTUP_OS_REPO &> /dev/null

    bazel_build ${target} ${force_compile}

    eval "${binary_for_target} ${args}"
    return_code="$?"
    popd &> /dev/null
    return ${return_code}
}

function _aa_completions()
{
  # COMP_WORDS is an array of words in the current command line.
  # COMP_WORDS[COMP_CWORD] is the word the cursor is on.
  local cur_word prev_word
  cur_word="${COMP_WORDS[COMP_CWORD]}"
  prev_word="${COMP_WORDS[COMP_CWORD-1]}"
  COMPREPLY=( $(bazel_run //tools/reviewer/aa:aa_script_helper 0 completions \"${prev_word}\" \"${cur_word}\") )
  return 0
}

# Find base folder based on existence of BASE file, and put it in AA_BASE
function set_AA_BASE {
  CWD=`pwd`
  while [[ `pwd` != / ]]; do
    if [[ -f `pwd`/BASE ]]; then
      AA_BASE=`pwd`
      cd $CWD
      return 0
    else
      cd ..
    fi
  done
  cd ${CWD}
  return 0
}

function aa {
  local AA_BINARY AA_RESULT AA_FORCE_COMPILE RED GREEN RESET
  RED=$(tput setaf 1)
  GREEN=$(tput setaf 2)
  RESET=$(tput sgr0)  
  set_AA_BASE
  if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1
  fi

  set_STARTUP_OS_REPO 1
  # `start_server` relies on having already-built version of local_server
  bazel_build //tools/reviewer/local_server:local_server
  bazel_run //tools/reviewer/aa:aa_script_helper 0 start_server $AA_BASE/head/startup-os

  if [[ "$1" = "workspace" ]]; then
      # For workspace command, `aa` prints commands to stdout, such as cd, that we need to execute.
      AA_RESULT=$( bazel_run //tools/reviewer/aa:aa_tool 0 $* )
      AA_RESULT_CODE=$?
      $AA_RESULT
  else
      # If command is not workspace, let `aa` execute as is
      bazel_run //tools/reviewer/aa:aa_tool 0 $*
      AA_RESULT_CODE=$?
  fi
  unset AA_BASE
  return ${AA_RESULT_CODE}
}

function aaw(){
  aa workspace $*
  # Workspace command succeeded, and was "-f"
  if [[ $? -eq 0 ]] && [[ $* == -f* ]]; then
    aa diff
  fi
}

# Make aa and aaw available as commands
export -f aa
export -f aaw
complete -F _aa_completions aa
complete -F _aa_completions aaw
