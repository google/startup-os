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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/** Class allowing to interact with system utils such as `bash`. */
public class CommandLine {
  /** Result of command execution. */
  public static class CommandResult {
    public String command;
    public String stdout;
    public String stderr;
    public Integer exitValue;
  }

  /**
   * Consumes lines from input stream; returns result as String.
   *
   * @param inputStream stream to consume lines from
   * @return String containing lines from stream
   * @throws IOException I/O error with inputStream
   */
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

  /**
   * Consumes stdout, stderr and exit code of command execution.
   *
   * @param command command to run
   * @return command execution result
   */
  public static CommandResult runCommandForError(String command, String workingDirectory) {
    CommandResult result = new CommandResult();
    try {
      Process process =
          new ProcessBuilder(Arrays.asList(command.split(" ")))
              .directory(new File(workingDirectory))
              .redirectErrorStream(true) // redirects stderr to stdout
              .start();
      result.exitValue = process.waitFor();
      // stdout and stderr are merged together
      result.stderr = result.stdout = readLines(process.getInputStream());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      // Thrown exception means command execution was not successful.
      result.exitValue = 1;
    }
    return result;
  }
}

