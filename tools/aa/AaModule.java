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

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.aa.Protos.Config;
import com.google.startupos.tools.aa.commands.InitCommand;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Named;

@Module(includes = CommonModule.class)
public class AaModule {
  public @Provides @Named("Base path") String provideBasePath(FileUtils fileUtils) {
    String currentFolder = fileUtils.getCurrentWorkingDirectory();
    while (currentFolder != null) {
      if (fileUtils.fileExists(fileUtils.joinPaths(currentFolder, InitCommand.BASE_FILENAME))) {
        return currentFolder;
      }
      File file = new File(currentFolder);
      currentFolder = file.getAbsoluteFile().getParent();
    }
    throw new IllegalStateException(
      String.format(
          "BASE file not found from %s until root", fileUtils.getCurrentWorkingDirectory()));
  }

  @Provides
  public static Config getConfig(FileUtils fileUtils, @Named("Base path") String basePath) {
    Config config =
        (Config)
            fileUtils.readPrototxtUnchecked(
                fileUtils.joinPaths(basePath, InitCommand.BASE_FILENAME), Config.newBuilder());
    config = config.toBuilder().setBasePath(basePath).build();
    return config;
  }

  @Provides
  @Named("Current workspace name")
  public static String getCurrentWorkspaceName(
      FileUtils fileUtils, @Named("Base path") String basePath) {
    Path currentFolder = Paths.get(fileUtils.getCurrentWorkingDirectory());
    Path wsFolder = Paths.get(fileUtils.joinPaths(basePath, "ws"));
    try {
      Path currentWsFolder = wsFolder.relativize(currentFolder).normalize();
      String wsName = currentWsFolder.subpath(0, 1).toString();
      if (wsName.equals("..")) {
        throw new RuntimeException("You're inside base folder but not in a workspace");
      }
      return wsName;
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("You're not in a workspace");
    }
  }

  /**
   * Parse string representing a positive integer or minus one
   *
   * @param s string to parse
   * @return parsed value or -1
   */
  private static Integer safeParsePositiveInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  @Provides
  @Named("Current diff number")
  public static Integer currentDiffNumber(
      FileUtils fileUtils,
      @Named("Current workspace name") String currentWorkspaceName,
      GitRepoFactory gitRepoFactory,
      Config config) {
    String workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);
    try {
      String firstWorkspacePath =
          fileUtils
              .listContents(workspacePath)
              .stream()
              .map(path -> fileUtils.joinPaths(workspacePath, path))
              .filter(fileUtils::folderExists)
              .findFirst()
              .orElse(null);

      if (firstWorkspacePath == null) {
        throw new RuntimeException(
            String.format("There are no repositories in workspace %s", workspacePath));
      }

      GitRepo repo = gitRepoFactory.create(firstWorkspacePath);
      return repo.listBranches()
          .stream()
          .filter(branchName -> branchName.startsWith("D"))
          .mapToInt(branchName -> safeParsePositiveInt(branchName.replace("D", "")))
          .filter(number -> number > 0)
          .findFirst()
          .orElse(-1);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
