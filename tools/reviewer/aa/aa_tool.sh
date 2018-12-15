#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from base/head repo)
# echo "source `pwd`/startup-os/tools/reviewer/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile

# Debugging:
# To compile aa from a workspace: 'export AA_FORCE_COMPILE_WS=<>'
# To undo: 'unset AA_FORCE_COMPILE_WS'

# TODO:
# 1. Add function set_BAZEL_WORKSPACE(), to set variable to either head or debug ws.
# 2. Add function bazel_run(force_build) to run a bazel command:
#    1. cd into workspace
#    2. Check if file exists
#    3. If not (or if force_build), bazel build it
#    4. Exit if build fails
#    5. Run command
# 3. Use bazel_run() in _aa_completions() and in aa(), set_BAZEL_WORKSPACE() in aa()
# 4. Make sure that after this, `aa` can be run from outside of a workspace (but inside base).

function _aa_completions()
{
  # COMP_WORDS is an array of words in the current command line.
  # COMP_WORDS[COMP_CWORD] is the word the cursor is on.
  local cur_word prev_word
  cur_word="${COMP_WORDS[COMP_CWORD]}"
  prev_word="${COMP_WORDS[COMP_CWORD-1]}"
  bazel build //tools/reviewer/aa:aa_script_helper &> /dev/null
  COMMAND="bazel-bin/tools/reviewer/aa/aa_script_helper completions \"${prev_word}\" \"${cur_word}\""
  COMPREPLY=( $(eval $COMMAND) )
  return 0
}

# Find base folder based on existence of BASE file, and put it in AA_BASE
function set_AA_BASE {
  CWD=`pwd`
  while [[ `pwd` != / ]]; do
    if [ -f `pwd`/BASE ]; then
      AA_BASE=`pwd`
      cd $CWD
      return 0
    else
      cd ..
    fi
  done
  cd $CWD
  return 0
}

function aa {
  local AA_BINARY AA_RESULT AA_FORCE_COMPILE RED GREEN RESET
  RED=$(tput setaf 1)
  GREEN=$(tput setaf 2)
  RESET=$(tput sgr0)  
  CWD=`pwd`
  set_AA_BASE
  if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1
  fi
  bazel run //tools/reviewer/aa:aa_script_helper -- start_server $AA_BASE/ws/simplify_aa/startup-os
  STARTUP_OS=$AA_BASE/head/startup-os

  AA_BINARY="$STARTUP_OS/bazel-bin/tools/reviewer/aa/aa_tool"
  if [ ! -z "$AA_FORCE_COMPILE_WS" ]; then
    echo "$RED[DEBUG]: building aa from ws $AA_BASE/ws/$AA_FORCE_COMPILE_WS/startup-os/$RESET"
    cd $AA_BASE/ws/$AA_FORCE_COMPILE_WS/startup-os/
    bazel build //tools/reviewer/aa:aa_tool
    if [ $? -ne 0 ]; then
      cd $CWD
      return 1
    fi
    AA_BINARY="$AA_BASE/ws/$AA_FORCE_COMPILE_WS/startup-os/bazel-bin/tools/reviewer/aa/aa_tool"
    cd $CWD
  elif [ ! -f $AA_BINARY ]; then
    cd $STARTUP_OS
    bazel build //tools/reviewer/aa:aa_tool
    if [ $? -ne 0 ]; then
      cd $CWD
      return 1
    fi
    AA_BINARY="$STARTUP_OS/bazel-bin/tools/reviewer/aa/aa_tool"
    cd $CWD
  fi

  if [ "$1" = "workspace" ]; then
      # For workspace command, `aa` prints commands to stdout, such as cd, that we need to execute.
      AA_RESULT=$(eval $AA_BINARY $*)
      AA_RESULT_CODE=$?
      $AA_RESULT
  else
      # If command is not workspace, let `aa` execute as is
      eval $AA_BINARY $*
      AA_RESULT_CODE=$?
  fi
  unset AA_BASE
  unset CWD
  return $AA_RESULT_CODE
}

function aaw(){
  aa workspace $*
  # Workspace command succeeded, and was "-f"
  if [ $? -eq 0 ] && [[ $* == -f* ]]; then
    aa diff
  fi
}

# Make aa and aaw available as commands
export -f aa
export -f aaw
complete -F _aa_completions aa
complete -F _aa_completions aaw
