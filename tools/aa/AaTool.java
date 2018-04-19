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

package com.google.startupos.tools.aa;

import com.google.startupos.tools.aa.commands.AaCommand;
import com.google.startupos.tools.aa.commands.InitCommand;
import com.google.startupos.tools.aa.Protos.Config;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flags;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

/** aa logic. */
public class AaTool {
  private static HashMap<String, AaCommand> commands = new HashMap<>();

  static {
    AaCommand init = new InitCommand();
    commands.put(init.getName(), init);
  }

  public static void printUsage() {
    System.out.println(
        String.format(
            "Invalid usage; available commands are %s", String.join(", ", commands.keySet())));
    Flags.printUsage();
  }

  public static void main(String[] args) {
    String[] leftOverArgs =
        Flags.parse(
            args,
            Arrays.asList(
              AaTool.class.getPackage().getName(), AaCommand.class.getPackage().toString()));

    if (leftOverArgs.length == 1) {
      String command = leftOverArgs[0];
      if (commands.containsKey(command)) {
        commands.get(command).run();
      } else {
        System.out.println("");
        printUsage();
      }
    } else {
      printUsage();
    }
  }
}

