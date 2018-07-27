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
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.common.repo.Protos.File;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.stream.Collectors;

public class SnapshotCommand implements AaCommand {
  private final FileUtils fileUtils;
  private final GitRepoFactory gitRepoFactory;
  private String workspacePath;
  private Integer diffNumber;

  private static final Integer GRPC_PORT = 8001;

  @Inject
  public SnapshotCommand(
      FileUtils fileUtils,
      GitRepoFactory repoFactory,
      @Named("Workspace path") String workspacePath,
      @Named("Diff number") Integer diffNumber) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = repoFactory;
    this.workspacePath = workspacePath;
    this.diffNumber = diffNumber;

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean run(String[] args) {
    if (diffNumber == -1) {
      System.out.println(RED_ERROR + "Cannot find diff number");
      return false;
    }
    String branchName = String.format("D%d", diffNumber);
    GitRepo gitRepo = this.gitRepoFactory.create(workspacePath);
    String initialBranch = gitRepo.currentBranch();
    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinPaths(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                GitRepo repo = this.gitRepoFactory.create(path);
                repo.switchBranch(branchName);
                ImmutableList<File> files = repo.getUncommittedFiles();
                if (files.isEmpty()) {
                  System.out.println(String.format("[%s]: No files to update", repoName));
                  return; // Only skips this iteration
                }
                String message =
                    branchName
                        + ":\n"
                        + files.stream().map(File::getFilename).collect(Collectors.joining("\n"));
                repo.commit(files, String.format(message, branchName));
                System.out.println(String.format("[%s]: Committed changes", repoName));
              });
    } catch (IOException e) {
      if (!gitRepo.currentBranch().equals(initialBranch)) {
        gitRepo.switchBranch(initialBranch);
      }
      e.printStackTrace();
    }
    return true;
  }
}

