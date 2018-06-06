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
import com.google.protobuf.Empty;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.tools.aa.Protos.Config;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.Author;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.DiffNumberResponse;
import com.google.startupos.tools.reviewer.service.Protos.File;
import com.google.startupos.tools.reviewer.service.Protos.DiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Reviewer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.stream.Collectors;

public class SnapshotCommand implements AaCommand {
  private FileUtils fileUtils;
  private Config config;
  private GitRepoFactory gitRepoFactory;
  private String currentWorkspaceName;
  private String workspacePath;
  private Integer diffNumber;

  private static final Integer GRPC_PORT = 8001;

  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;

  @Inject
  public SnapshotCommand(
      FileUtils fileUtils,
      Config config,
      GitRepoFactory repoFactory,
      @Named("Current workspace name") String currentWorkspaceName,
      @Named("Current diff number") Integer diffNumber) {
    this.fileUtils = fileUtils;
    this.config = config;
    this.gitRepoFactory = repoFactory;
    this.currentWorkspaceName = currentWorkspaceName;
    this.workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);
    this.diffNumber = diffNumber;

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }
  @Override
  public boolean run(String[] args) {
    if (diffNumber == -1) {
      System.out.println(RED_ERROR + "Cannot find diff number");
      return false;
    }
    String branchName = String.format("D%d", diffNumber);

    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinPaths(workspacePath, path))
          .filter(path -> fileUtils.folderExists(path))
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                GitRepo repo = this.gitRepoFactory.create(path);
                repo.switchBranch(branchName);
                ImmutableList<File> files = repo.getUncommittedFiles();
                if (files.isEmpty()) {
                  System.out.println(
                      String.format("[%s]: No files to update", repoName));
                  return; // Only skips this iteration
                }
                String message = branchName + ":\n" +
                    files.stream().map(f -> f.getFilename()).collect(Collectors.joining("\n"));
                Commit commit = repo.commit(files, String.format(message, branchName));
                System.out.println(String.format("[%s]: Committed changes", repoName));
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }
}
