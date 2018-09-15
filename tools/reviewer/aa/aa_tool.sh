#!/usr/bin/env bash

# To setup `aa` tool, you need to execute (from base/head repo)
# echo "source `pwd`/startup-os/tools/reviewer/aa/aa_tool.sh" >> ~/.bashrc
# source ~/.bashrc
#
# If you're on macOS, substitute ~/.bashrc with ~/.bash_profile

# Debugging:
# To compile aa from a workspace: 'export AA_FORCE_COMPILE_WS=<>'
# To undo: 'unset AA_FORCE_COMPILE_WS'

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
RESET=$(tput sgr0)


function _aa_completions()
{
    local cur_word prev_word type_list

    # COMP_WORDS is an array of words in the current command line.
    # COMP_CWORD is the index of the current word (the one the cursor is
    # in). So COMP_WORDS[COMP_CWORD] is the current word; we also record
    # the previous word here
    cur_word="${COMP_WORDS[COMP_CWORD]}"
    prev_word="${COMP_WORDS[COMP_CWORD-1]}"

    commands="init workspace diff fix sync snapshot add_repo killserver"
    init_options="--base_path --startupos_repo --user"
    add_repo_options="--url --name"
    diff_options="--reviewers --description --buglink"

    if [ "$prev_word" = "aa" ] ; then
        # completing command name
        unset command
        COMPREPLY=( $(compgen -W "${commands}" -- ${cur_word}) )
    elif [ "$prev_word" = "workspace" ] || [ "$prev_word" = "aaw" ] ; then
        # completing names of workspaces
        find_base_folder
        workspaces=$(ls -1 $AA_BASE/ws/)
        COMPREPLY=( $(compgen -W "${workspaces}" -- "${cur_word}") )
    elif echo $commands | grep --quiet -- "$prev_word"; then
        # user entered "aa <command>" already
        command="$prev_word"
    else
        COMPREPLY=()
    fi

    if [[ ${cur_word} == -* ]] ; then
        if [[ $command = "init" ]]; then
          # completing params for `init`
          COMPREPLY=( $(compgen -W "${init_options}" -- ${cur_word}) )
        elif [[ $command = "add_repo" ]]; then
          # completing params for `add_repo`
          COMPREPLY=( $(compgen -W "${add_repo_options}" -- ${cur_word}) )
        elif [[ $command = "diff" ]]; then
          # completing params for `diff`
          COMPREPLY=( $(compgen -W "${diff_options}" -- ${cur_word}) )
        fi
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

function stop_local_server {
    find_base_folder
    if [[ -z "$AA_BASE" ]]; then
      echo "BASE file not found in path until root"
      return 1
    fi

    export STARTUP_OS=$AA_BASE/head/startup-os
    bash $AA_BASE/head/startup-os/tools/reviewer/local_server/local_server.sh stop
}

function start_local_server {
    # starts local_server if it is not running yet
    # server is started by tools/reviewer/local_server/local_server.sh
    # to check whether it is running already we try
    # to communicate with it by gRPC via polyglot
    # for additional output, set LOCAL_SERVER_POLYGLOT_DEBUG env variable

    find_base_folder
    if [[ -z "$AA_BASE" ]]; then
      echo "BASE file not found in path until root"
      return 1
    fi

    export STARTUP_OS=$AA_BASE/head/startup-os

    # store stderr to debug
    POLYGLOT_STDERR_DEBUG_FILE=$AA_BASE/logs/polyglot_stderr.log
    SERVER_LOG_FILE=$AA_BASE/logs/server.log
    # call `ping` method via gRPC and store result (ignored for now)
    pushd $STARTUP_OS >/dev/null
    OUTPUT=$(echo {} | bazel run //tools:grpc_polyglot -- \
      --command=call \
      --endpoint=localhost:8001 \
      --full_method=com.google.startupos.tools.reviewer.localserver.service.CodeReviewService/ping \
      2>$POLYGLOT_STDERR_DEBUG_FILE)
    POLYGLOT_EXIT_CODE=$?
    popd >/dev/null

    # if polyglot cannot reach gRPC server it exits with nonzero code
    # then we need to run server
    if [ $POLYGLOT_EXIT_CODE -ne 0 ]; then
        if [ ! -z "$LOCAL_SERVER_POLYGLOT_DEBUG" ]; then
          echo "$RED[DEBUG]: Polyglot exit code was $POLYGLOT_EXIT_CODE$RESET"
          echo "$RED[DEBUG]: Polyglot output was $RESET$OUTPUT"
          echo "$RED[DEBUG]: Polyglot stderr is stored at $RESET$POLYGLOT_STDERR_DEBUG_FILE"
        fi
        echo "$GREEN""Local server did not respond, starting it...$RESET"
        echo "$GREEN""Server PID is:$RESET" # will be printed by bash
        # nohup detaches the command from terminal it was executed on
        echo "$GREEN""Server log is $SERVER_LOG_FILE"
        nohup bash $AA_BASE/head/startup-os/tools/reviewer/local_server/local_server.sh start </dev/null >$SERVER_LOG_FILE 2>&1 &
        echo "$RED""Visit$RESET http://localhost:8000$RED to log in$RESET"
        return 1
    else
       # polyglot reached gRPC server so it is running already
       if [ ! -z "$LOCAL_SERVER_POLYGLOT_DEBUG" ]; then
          echo "$GREEN[DEBUG]: Server is already running, nothing to do$RESET"
          echo "$RED[DEBUG]: Polyglot exit code was $POLYGLOT_EXIT_CODE$RESET"
          echo "$RED[DEBUG]: Polyglot output was $RESET$OUTPUT"
          echo "$RED[DEBUG]: Polyglot stderr is stored at $RESET$POLYGLOT_STDERR_DEBUG_FILE"
       fi
    fi
}

function aa {
  CWD=`pwd`
  find_base_folder
  if [[ -z "$AA_BASE" ]]; then
    echo "BASE file not found in path until root"
    return 1
  fi

  start_local_server
  if [ ! $? -eq 0 ]; then
    echo "$GREEN""Please execute the same command (shorthand: $RESET!!$GREEN) after server starts$RESET";
    return 1
  fi

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
      # For workspace command, instead of letting `aa` print to stdout
      # we need to capture its output and execute it as command
      AA_RESULT=$(eval $AA_BINARY $*)
      AA_RESULT_CODE=$?
      $AA_RESULT
  elif [ "$1" = "killserver" ]; then
      stop_local_server
  else
      # if command is not workspace, let `aa` execute as is
      eval $AA_BINARY $*
      AA_RESULT_CODE=$?
  fi
  unset AA_BASE
  unset AA_BINARY
  unset AA_RESULT
  unset CWD
  unset AA_FORCE_COMPILE
  return $AA_RESULT_CODE
}

function aaw(){
  aa workspace $*
  # Workspace command succeeded, and was "-f"
  if [ $? -eq 0 ] && [[ $* == -f* ]]; then
    aa diff
  fi
}

# make aa available as command
export -f aa
export -f aaw
complete -F _aa_completions aa
complete -F _aa_completions aaw
