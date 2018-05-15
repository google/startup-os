#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from base/head repo)
# echo "source `pwd`/startup-os/tools/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile

# Find base folder based on existence of BASE file, and put it in AA_BASE
function find_base_folder {
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
  CWD=`pwd`
  find_base_folder
  if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1
  fi
  STARTUP_OS=$AA_BASE/head/startup-os

  # Uncomment to override StartupOS repo:
  #STARTUP_OS=<repo path>
  # Uncomment to force recompile:
  #AA_FORCE_COMPILE=1

  AA_BINARY="$STARTUP_OS/bazel-bin/tools/aa/aa_tool"
  if [ ! -f $AA_BINARY ] || [ "$AA_FORCE_COMPILE" = "1" ]; then
    cd $STARTUP_OS
    bazel build //tools/aa:aa_tool
    if [ $? -ne 0 ]; then
      cd $CWD
      return 1
    fi
    cd $CWD
  fi
  if [ "$1" = "workspace" ]; then
      # For workspace command, instead of letting `aa` print to stdout
      # we need to capture its output and execute it as command
      AA_RESULT=$(eval $AA_BINARY $*)
      $AA_RESULT
  else
      # if command is not workspace, let `aa` execute as is
      eval $AA_BINARY $*
  fi
  unset AA_BASE
  unset AA_BINARY
  unset AA_RESULT
  unset CWD
  unset AA_FORCE_COMPILE
}

# make aa available as command
export -f aa
