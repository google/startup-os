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

package com.google.startupos.tools.reviewer.aa.commands;

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.RegistryProtos.ReviewerRegistry;
import com.google.startupos.tools.reviewer.RegistryProtos.ReviewerRegistryConfig;

import javax.inject.Inject;

/* A command to init a base folder.
 *
 * Usage:
 * bazel run //tools/reviewer/aa:aa_tool -- init --base_path </path/to/base/folder>
 * or, if aa is already set up:
 * aa init --base_path </path/to/base/folder>
 */
public class InitCommand implements AaCommand {
  public static final String BASE_FILENAME = "BASE";

  @FlagDesc(name = "base_path", description = "Base path for workspaces, head etc.")
  public static Flag<String> basePath = Flag.create("");

  @FlagDesc(name = "startupos_repo", description = "StartupOS git repo")
  public static Flag<String> startuposRepo =
      Flag.create("https://github.com/google/startup-os.git");

  @FlagDesc(name = "global_registry_config", description = "Path to global_registry.prototxt")
  public static Flag<String> globalRegistryConfig = Flag.create("");

  private final GitRepoFactory gitRepoFactory;
  private FileUtils fileUtils;
  private boolean baseFolderExistedBefore = true;

  @Inject
  public InitCommand(FileUtils fileUtils, GitRepoFactory gitRepoFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
  }

  private void cloneRepoIntoHead(String dirName, String repoUrl) {
    String startupOsPath = fileUtils.joinToAbsolutePath(basePath.get(), "head", dirName);
    System.out.println("Cloning StartupOS into " + startupOsPath);
    GitRepo repo = this.gitRepoFactory.create(startupOsPath);
    repo.cloneRepo(repoUrl, startupOsPath);
    System.out.println("Completed Cloning");
  }

  public boolean run(String basePath, String startuposRepo) {
    if (basePath.isEmpty()) {
      throw new IllegalArgumentException("--base_path must be set");
    }
    try {
      if (!fileUtils.folderEmptyOrNotExists(basePath)) {
        System.out.println("Base folder exists and is not empty");
        return false;
      }
      baseFolderExistedBefore = fileUtils.folderExists(basePath);
      // Create folders
      fileUtils.mkdirs(fileUtils.joinToAbsolutePath(basePath, "head"));
      fileUtils.mkdirs(fileUtils.joinToAbsolutePath(basePath, "ws"));
      fileUtils.mkdirs(fileUtils.joinToAbsolutePath(basePath, "local"));
      fileUtils.mkdirs(fileUtils.joinToAbsolutePath(basePath, "logs"));

      // Write BASE file
      fileUtils.writeString("", fileUtils.joinToAbsolutePath(basePath, BASE_FILENAME));

      if (!startuposRepo.isEmpty()) {
        // Clone StartupOS repo into head:
        cloneRepoIntoHead("startup-os", startuposRepo);
      } else {
        System.out.println("Warning: StartupOS repo url is empty. Cloning skipped.");
      }

      if (!globalRegistryConfig.get().isEmpty()) {
        ReviewerRegistry registry =
            (ReviewerRegistry)
                fileUtils.readPrototxtUnchecked(
                    fileUtils.joinToAbsolutePath(
                        fileUtils.getCurrentWorkingDirectory(), globalRegistryConfig.get()),
                    ReviewerRegistry.newBuilder());
        for (ReviewerRegistryConfig config : registry.getReviewerConfigList()) {
          if (config.getConfigRepo().equals(startuposRepo)) {
            // should not clone StartupOS twice
          } else {
            cloneRepoIntoHead(config.getId(), config.getConfigRepo());
          }
        }
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

  @Override
  public boolean run(String[] args) {
    Flags.parseCurrentPackage(args);
    return run(basePath.get(), startuposRepo.get());
  }

  private void revertChanges() {
    if (baseFolderExistedBefore) {
      fileUtils.clearDirectoryUnchecked(basePath.get());
    } else {
      fileUtils.deleteDirectoryUnchecked(basePath.get());
    }
  }
}

