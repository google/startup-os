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

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.aa.Protos.Config;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;

public class SyncCommand implements AaCommand {

  private static final String TEMP_BRANCH_FOR_SYNC = "temp_branch_for_sync";

  private final FileUtils fileUtils;
  private final Config config;
  private GitRepoFactory repoFactory;
  private String currentWorkspaceName;

  @Inject
  public SyncCommand(
      FileUtils utils,
      Config config,
      GitRepoFactory repoFactory,
      @Named("Current workspace name") String currentWorkspaceName) {
    this.fileUtils = utils;
    this.config = config;
    this.repoFactory = repoFactory;
    this.currentWorkspaceName = currentWorkspaceName;
  }

  @Override
  public boolean run(String[] args) {
    String headPath = fileUtils.joinPaths(this.config.getBasePath(), "head");

    // Pull all repos in head
    try {
      fileUtils
          .listContents(headPath)
          .stream()
          .map(path -> fileUtils.joinPaths(headPath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                System.out.println(String.format("[HEAD]: Performing sync: %s", path));
                repoFactory.create(path).pull();
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    // then, do the sync for all workspaces
    String workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);
    boolean wasTempBranchForSyncExisted =
        repoFactory.create(workspacePath).listBranches().contains(TEMP_BRANCH_FOR_SYNC);
    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinPaths(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                System.out.println(
                    String.format("[%s/%s]: Performing sync", currentWorkspaceName, repoName));
                GitRepo repo = repoFactory.create(path);
                System.out.println(
                    String.format(
                        "[%s/%s]: switching to temp branch", currentWorkspaceName, repoName));
                repo.switchBranch(TEMP_BRANCH_FOR_SYNC);
                System.out.println(
                    String.format(
                        "[%s/%s]: committing all changes", currentWorkspaceName, repoName));
                repo.commit(repo.getUncommittedFiles(), "Sync: temporary commit");
                System.out.println(
                    String.format("[%s/%s]: switching to master", currentWorkspaceName, repoName));
                repo.switchBranch("master");
                System.out.println(
                    String.format("[%s/%s]: pulling", currentWorkspaceName, repoName));
                repo.pull();
                System.out.println(
                    String.format("[%s/%s]: merging changes", currentWorkspaceName, repoName));
                boolean mergeResult = repo.merge("temp_branch_for_sync");
                if (!mergeResult) {
                  System.out.println(
                      String.format(
                          "[%s/%s]: manual merge required, check files for conflicts",
                          currentWorkspaceName, repoName));
                }
                System.out.println(
                    String.format("[%s/%s]: removing temp branch", currentWorkspaceName, repoName));
                repo.removeBranch("temp_branch_for_sync");
              });
    } catch (IOException e) {
      revertChanges(workspacePath, wasTempBranchForSyncExisted);
      e.printStackTrace();
    }
    return true;
  }

  private void revertChanges(String workspacePath, boolean wasTempBranchForSyncExist) {
    GitRepo repo = repoFactory.create(workspacePath);
    ImmutableList<String> branches = repo.listBranches();
    if (branches.contains(TEMP_BRANCH_FOR_SYNC) && !wasTempBranchForSyncExist) {
      repo.removeBranch(TEMP_BRANCH_FOR_SYNC);
    }
  }
}

