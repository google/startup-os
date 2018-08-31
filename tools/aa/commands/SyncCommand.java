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
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

public class SyncCommand implements AaCommand {

  private final FileUtils fileUtils;
  private GitRepoFactory repoFactory;
  private String headPath;
  private String workspaceName;
  private String workspacePath;
  private Map<String, String> repoToInitialBranch = new HashMap<>();

  @Inject
  public SyncCommand(
      FileUtils utils,
      GitRepoFactory repoFactory,
      @Named("Head path") String headPath,
      @Named("Workspace name") String workspaceName,
      @Named("Workspace path") String workspacePath) {
    this.fileUtils = utils;
    this.repoFactory = repoFactory;
    this.headPath = headPath;
    this.workspaceName = workspaceName;
    this.workspacePath = workspacePath;
  }

  @Override
  public boolean run(String[] args) {
    // Pull all repos in head
    try {
      fileUtils
          .listContents(headPath)
          .stream()
          .map(path -> fileUtils.joinToAbsolutePath(headPath, path))
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
    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinToAbsolutePath(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                System.out.println(
                    String.format("[%s/%s]: Performing sync", workspaceName, repoName));
                GitRepo repo = repoFactory.create(path);
                repoToInitialBranch.put(repoName, repo.currentBranch());
                System.out.println(
                    String.format("[%s/%s]: switching to temp branch", workspaceName, repoName));
                repo.switchBranch("temp_branch_for_sync");
                System.out.println(
                    String.format("[%s/%s]: committing all changes", workspaceName, repoName));
                repo.commit(repo.getUncommittedFiles(), "Sync: temporary commit");
                System.out.println(
                    String.format("[%s/%s]: switching to master", workspaceName, repoName));
                repo.switchBranch("master");
                System.out.println(String.format("[%s/%s]: pulling", workspaceName, repoName));
                repo.pull();
                System.out.println(
                    String.format("[%s/%s]: merging changes", workspaceName, repoName));
                boolean mergeResult = repo.merge("temp_branch_for_sync");
                if (!mergeResult) {
                  System.out.println(
                      String.format(
                          "[%s/%s]: manual merge required, check files for conflicts",
                          workspaceName, repoName));
                }
                System.out.println(
                    String.format("[%s/%s]: removing temp branch", workspaceName, repoName));
                repo.removeBranch("temp_branch_for_sync");
              });
    } catch (Exception e) {
      revertChanges(repoToInitialBranch);
      e.printStackTrace();
    }
    return true;
  }

  private void revertChanges(Map<String, String> repoToInitialBranch) {
    if (!repoToInitialBranch.isEmpty()) {
      repoToInitialBranch.forEach(
          (repoName, initialBranch) -> {
            GitRepo repo =
                repoFactory.create(fileUtils.joinToAbsolutePath(workspacePath, repoName));
            String currentBranch = repo.currentBranch();
            if (!currentBranch.equals(initialBranch)) {
              repo.switchBranch(initialBranch);
            }
          });
    }
  }
}

