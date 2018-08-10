/*
 * Copyright 2018 The StartupOS Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.startupos.tools.aa.commands;

public interface AaCommand {
  String ANSI_RED = "\u001B[31m";
  String ANSI_YELLOW = "\u001B[33m<new code>";
  String ANSI_BOLD = "\u001b[1m";
  String ANSI_RESET = "\u001B[0m";
  String RED_ERROR = ANSI_RED + ANSI_BOLD + "ERROR: " + ANSI_RESET;
  String YELLOW_NOTE = ANSI_YELLOW + ANSI_BOLD + "NOTE: " + ANSI_RESET;
  // Run command, return true on success.
  boolean run(String[] args);
  // TODO: implement ability to interrupt commands
  // with leaving workspace in defined state
}

