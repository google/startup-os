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

import javax.inject.Inject;

/* A command to init a base folder.
 *
 * Usage:
 * bazel run //tools/aa:aa_tool -- init --base_path </path/to/base/folder>
 * or, if aa is already set up:
 * aa init --base_path </path/to/base/folder>
 */
// TODO: Make aa init work without --base_path, i.e `aa init <base_path>`
public class InitCommand implements AaCommand {
  public static final String BASE_FILENAME = "BASE";

  @FlagDesc(name = "base_path", description = "Base path", required = true)
  public static Flag<String> basePath = Flag.create("");

  @FlagDesc(name = "startupos_repo", description = "StartupOS git repo")
  public static Flag<String> startuposRepo =
      Flag.create("https://github.com/google/startup-os.git");

  private final GitRepoFactory gitRepoFactory;
  private FileUtils fileUtils;

  @Inject
  public InitCommand(FileUtils fileUtils, GitRepoFactory gitRepoFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
  }

  @Override
  public boolean run(String[] args) {
    // TODO: Add Flags.parse() support for specifying a particular class, not a whole package
    Flags.parse(args, InitCommand.class.getPackage());

    try {
      if (!fileUtils.folderEmptyOrNotExists(basePath.get())) {
        System.out.println("Error: Base folder exists and is not empty");
        System.exit(1);
      }
      // Create folders
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "head"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "ws"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "local"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "logs"));

      // Write BASE file
      fileUtils.writeString("", fileUtils.joinPaths(basePath.get(), BASE_FILENAME));

      if (!startuposRepo.get().isEmpty()) {
        // Clone StartupOS repo into head:
        String startupOsPath = fileUtils.joinPaths(basePath.get(), "head", "startup-os");
        System.out.println("Cloning StartupOS into " + startupOsPath);
        GitRepo repo = this.gitRepoFactory.create(startupOsPath);
        repo.cloneRepo(startuposRepo.get(), fileUtils.joinPaths(startupOsPath, ".git"));
        System.out.println("Completed Cloning");
      } else {
        System.out.println("Warning: StartupOS repo url is empty. Cloning skipped.");
      }

    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Input flags:");
      Flags.printUsage();
      return false;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }
}

