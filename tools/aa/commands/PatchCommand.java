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
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

public class PatchCommand implements AaCommand {

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private String workspacePath;

  @FlagDesc(name = "diff_number", description = "Diff number to apply patch from", required = true)
  public static Flag<Integer> diffNumber = Flag.create(-1);

  @Inject
  public PatchCommand(
      FileUtils utils, GitRepoFactory repoFactory, @Named("Workspace path") String workspacePath) {
    this.fileUtils = utils;
    this.gitRepoFactory = repoFactory;
    this.workspacePath = workspacePath;
  }

  @Override
  public boolean run(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-d")) {
        args[i] = "--diff_number";
      }
    }

    Flags.parse(args, PatchCommand.class.getPackage());

    String branchName = String.format("D%d", diffNumber.get());
    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinPaths(workspacePath, path))
          .filter(path -> fileUtils.folderExists(path))
          .forEach(
              path -> {
                GitRepo repo = this.gitRepoFactory.create(path);
                String currentBranchName = repo.currentBranch();
                if (!currentBranchName.startsWith("D")) {
                  System.err.println(
                      String.format(
                          "Currently (%s) not on a diff branch, unable to apply changes from %s",
                          ANSI_YELLOW + ANSI_BOLD + currentBranchName + ANSI_RESET, branchName));
                } else {
                  repo.merge(branchName, true);
                }
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}

