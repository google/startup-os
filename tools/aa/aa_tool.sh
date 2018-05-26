#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from base/head repo)
# echo "source `pwd`/startup-os/tools/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile


function _aa_completions()
{
    local cur_word prev_word type_list

    # COMP_WORDS is an array of words in the current command line.
    # COMP_CWORD is the index of the current word (the one the cursor is
    # in). So COMP_WORDS[COMP_CWORD] is the current word; we also record
    # the previous word here
    cur_word="${COMP_WORDS[COMP_CWORD]}"
    prev_word="${COMP_WORDS[COMP_CWORD-1]}"

    commands="init workspace diff fix sync"
    init_options="--base_path --startupos_repo --user"

    if [ "$prev_word" = "aa" ] ; then
        # completing command name
        unset command
        COMPREPLY=( $(compgen -W "${commands}" -- ${cur_word}) )
    elif [ "$prev_word" = "workspace" ] || [ "$prev_word" = "aaw" ] ; then
        # completing names of workspaces
        find_base_folder
        workspaces=$(ls -1 $AA_BASE/ws/)
        COMPREPLY=( $(compgen -W "${workspaces}" -- "${cur_word}") )
    elif [ "$prev_word" = "init" ]; then
        # user entered "aa init" already
        command="init"
    else
        COMPREPLY=()
    fi

    if [[ $command = "init" && ${cur_word} == -* ]] ; then
        # completing params for `init`
        COMPREPLY=( $(compgen -W "${init_options}" -- ${cur_word}) )
    fi

    return 0
}

# Find base folder based on existence of BASE file, and put it in AA_BASE
function find_base_folder {
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
  CWD=`pwd`
  find_base_folder
  if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1
  fi
  STARTUP_OS=$AA_BASE/head/startup-os

  # Uncomment to override StartupOS repo:
  STARTUP_OS=~/devel/base/ws/aa_workspace_fixes/startup-os
  # Uncomment to force recompile:
  AA_FORCE_COMPILE=1

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
alias aaw="aa workspace"
complete -F _aa_completions aa
complete -F _aa_completions aaw
