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
import java.util.Arrays;
import javax.inject.Inject;

/**
 * This command is used to switch between workspaces and create new ones.
 *
 * Usage:
 * To switch to a workspace:
 * aa workspace <workspace name>
 *
 * To create and then switch to a workspace:
 * aa workspace -f <workspace name>
 */
// TODO: If there's only one repo in the workspace, cd should enter into it.
// TODO: If there's multiple repos in the workspace, and I'm currently in a repo in a workspace,
// cd should enter into that repo.
public class WorkspaceCommand implements AaCommand {
  @FlagDesc(name = "force", description = "Create workspace if it doesn't exist")
  public static Flag<Boolean> force = Flag.create(false);

  private FileUtils fileUtils;
  private Config config;

  @Inject
  public WorkspaceCommand(FileUtils fileUtils, Config config) {
    this.fileUtils = fileUtils;
    this.config = config;
  }

  // Guess folder to cd into after workspace command
  private String guessFolderForCd(String workspacePath) {
    // Try using current workspace subfolder
    String guessedRepoFolder = fileUtils.joinPaths(
      workspacePath, fileUtils.getCurrentWorkingDirectoryName());
    if (fileUtils.folderExists(guessedRepoFolder)) {
      return guessedRepoFolder;
    }
    // If target workspace has only 1 subfolder, use it
    try {
      if (fileUtils.listContents(workspacePath).size() == 1) {
        guessedRepoFolder = fileUtils.joinPaths(
            workspacePath, fileUtils.listContents(workspacePath).get(0));
        if (fileUtils.folderExists(guessedRepoFolder)) {
          return guessedRepoFolder;
        }
      }
    } catch(Exception e) {
      // Fail silently. It's just a nice-to-have.
    }
    return null;
  }

  @Override
  public boolean run(String[] args) {
    // Note: System.out gets executed by the calling aa_tool.sh to run commands such as cd.

    // The Flags library does not support short flags by design, as this increases the chance of
    // flag collisions between packages. To allow short flags here, we do a flag replacement.
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-f")) {
        args[i] = "--force";
      }
    }
    if (args.length < 2) {
      System.err.println(RED_ERROR + "Missing workspace name");
      return false;
    }

    String workspaceName = args[args.length - 1];
    if (workspaceName.startsWith("-")) {
      System.err.println(RED_ERROR + "Missing workspace name");
      return false;
    }

    args = Arrays.copyOfRange(args, 0, args.length - 1);

    Flags.parse(args, WorkspaceCommand.class.getPackage());
    String basePath = config.getBasePath();
    String workspacePath = fileUtils.joinPaths(basePath, "ws", workspaceName);

    if (force.get()) {
      if (fileUtils.folderExists(workspacePath)) {
        System.err.println(RED_ERROR + "Workspace already exists");
        return false;
      } else {
        fileUtils.mkdirs(workspacePath);
        try {
          fileUtils.copyDirectoryToDirectory(
              fileUtils.joinPaths(basePath, "head"), workspacePath, "^bazel-.*$");
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        }
      }
    } else {
      if (!fileUtils.folderExists(workspacePath)) {
        System.err.println(RED_ERROR + "Workspace does not exist");
        return false;
      }
    }
    // System.out command will be run by calling script aa_tool.sh.
    String guessedRepoFolder = guessFolderForCd(workspacePath);
    if (guessedRepoFolder != null) {
      System.out.println(String.format("cd %s", guessedRepoFolder));
    } else {
      System.out.println(String.format("cd %s", workspacePath));
    }
    return true;
  }
}
