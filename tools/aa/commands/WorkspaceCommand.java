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

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.aa.Protos.Config;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Inject;

/**
 * This command is used to switch between workspace and create new ones Its output gets executed in
 * aa function of aa_tool.sh to perform the switching
 */
public class WorkspaceCommand implements AaCommand {
  @FlagDesc(name = "force", description = "Create workspace if it doesn't exist")
  public static Flag<Boolean> force = Flag.create(false);

  private FileUtils fileUtils;
  private Config config;

  @Inject
  public WorkspaceCommand(FileUtils utils, Config config) {
    this.fileUtils = utils;
    this.config = config;
  }

  @Override
  public void run(String[] args) {
    /*
     * //common/flags library intentionally does not support short flags
     * as this would introduce ambiguity between multiple packages.
     * Before parsing flags, we replace "-f" with "--force" so we are
     * able to call the tool as `aa workspace -f <wsname>`
     */
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-f")) {
        args[i] = "--force";
      }
    }

    String workspaceName = args[args.length - 1];

    if (workspaceName.equals(getName()) || workspaceName.startsWith("-")) {
      throw new IllegalArgumentException("Supply workspace name");
    }

    args = Arrays.copyOfRange(args, 0, args.length - 1);

    Flags.parse(args, WorkspaceCommand.class.getPackage());
    String basePath = config.getBasePath();

    String headPath = Paths.get(basePath, "head").toAbsolutePath().toString();
    String newWsPath =
        Paths.get(basePath, config.getUser(), "ws", workspaceName).toAbsolutePath().toString();

    if (force.get()) {
      fileUtils.mkdirs(newWsPath);
      try {
        fileUtils.copyDirectoryToDirectory(headPath, newWsPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // output change-directory command to be executed by aa (shell function)
    System.out.println(String.format("cd %s", newWsPath));
  }

  @Override
  public String getName() {
    return "workspace";
  }
}
