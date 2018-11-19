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

package com.google.startupos.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CommandLine {
  public static class CommandResult {
    public String command;
    public String stdout;
    public String stderr;
    public Integer exitValue;
  }

  private static String readLines(InputStream inputStream) throws IOException {
    StringBuffer output = new StringBuffer();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append('\n');
      }
    }
    return output.toString();
  }

  public static CommandResult runCommandForError(String command) {
    CommandResult result = new CommandResult();
    try {
      Process process =
          new ProcessBuilder(Arrays.asList(command.split(" "))).redirectErrorStream(true).start();
      result.stderr = readLines(process.getInputStream());
      result.exitValue = process.exitValue();
    } catch (IOException e) {
      e.printStackTrace();
      // thrown exception obviously means command execution was not successul
      result.exitValue = 1;
    }
    return result;
  }
}

