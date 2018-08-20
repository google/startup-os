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
 * bazel run //tools/aa:aa_tool -- init </path/to/base/folder>
 * or, if aa is already set up:
 * aa init </path/to/base/folder>
 */
public class InitCommand implements AaCommand {
  public static final String BASE_FILENAME = "BASE";

  // set by processArgs
  private String basePath;

  @FlagDesc(name = "startupos_repo", description = "StartupOS git repo")
  public static Flag<String> startuposRepo =
      Flag.create("https://github.com/google/startup-os.git");

  private final GitRepoFactory gitRepoFactory;
  private FileUtils fileUtils;
  private boolean baseFolderExistedBefore = true;

  @Inject
  public InitCommand(FileUtils fileUtils, GitRepoFactory gitRepoFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
  }

  @Override
  public boolean run(String[] args) {
    if (!processArgs(args)) {
      return false;
    }
    // TODO: Add Flags.parse() support for specifying a particular class, not a whole package
    Flags.parse(args, InitCommand.class.getPackage());
    try {
      if (!fileUtils.folderEmptyOrNotExists(basePath)) {
        System.out.println("Error: Base folder exists and is not empty");
        System.exit(1);
      }
      baseFolderExistedBefore = fileUtils.folderExists(basePath);
      // Create folders
      fileUtils.mkdirs(fileUtils.joinPaths(basePath, "head"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath, "ws"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath, "local"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath, "logs"));

      // Write BASE file
      fileUtils.writeString("", fileUtils.joinPaths(basePath, BASE_FILENAME));

      if (!startuposRepo.get().isEmpty()) {
        // Clone StartupOS repo into head:
        String startupOsPath = fileUtils.joinPaths(basePath, "head", "startup-os");
        System.out.println("Cloning StartupOS into " + startupOsPath);
        GitRepo repo = this.gitRepoFactory.create(startupOsPath);
        repo.cloneRepo(startuposRepo.get(), startupOsPath);
        System.out.println("Completed Cloning");
      } else {
        System.out.println("Warning: StartupOS repo url is empty. Cloning skipped.");
      }

    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Input flags:");
      Flags.printUsage();
      revertChanges();
      return false;
    } catch (Exception e) {
      revertChanges();
      e.printStackTrace();
    }
    return true;
  }

  private boolean processArgs(String[] args) {
    if (args.length == 0 || args.length == 1) {
      System.err.println(
          RED_ERROR
              + "Invalid usage. \n"
              + "Available variants are: \"bazel run //tools/aa:aa_tool -- init </path/to/base/folder>\" "
              + "or, if aa is already set up: \"aa init <base_path>\"");
      return false;
    }
    boolean aaIsAlreadySetUpMode = args[0].equals("aa");
    if (aaIsAlreadySetUpMode) {
      if (args.length < 3) {
        System.err.println(RED_ERROR + "Missing base_path");
        return false;
      } else {
        basePath = args[2];
      }
    } else {
      basePath = args[1];
    }
    return true;
  }

  private void revertChanges() {
    if (baseFolderExistedBefore) {
      fileUtils.clearDirectoryUnchecked(basePath);
    } else {
      fileUtils.deleteDirectoryUnchecked(basePath);
    }
  }
}

