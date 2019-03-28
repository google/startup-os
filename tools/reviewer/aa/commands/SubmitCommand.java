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
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.local_server.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.local_server.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Diff;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Diff.Status;
import com.google.startupos.tools.reviewer.local_server.service.Protos.DiffRequest;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Reviewer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;

public class SubmitCommand implements AaCommand {

  private final FileUtils fileUtils;
  private final GitRepoFactory gitRepoFactory;
  private String workspacePath;
  private Integer diffNumber;

  private static final Integer GRPC_PORT = 8001;

  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;

  @Inject
  public SubmitCommand(
      FileUtils utils,
      GitRepoFactory repoFactory,
      @Named("Workspace path") String workspacePath,
      @Named("Diff number") Integer diffNumber) {
    this.fileUtils = utils;
    this.gitRepoFactory = repoFactory;
    this.workspacePath = workspacePath;
    this.diffNumber = diffNumber;

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean run(String[] args) {
    if (diffNumber == -1) {
      System.out.println(
          RED_ERROR + "Workspace has no diff to submit (git branch has no D# branch)");
      return false;
    }

    final Diff.Builder diffBuilder =
        codeReviewBlockingStub
            .getDiff(DiffRequest.newBuilder().setDiffId(diffNumber).build())
            .toBuilder();

    boolean hasApprovedReviews =
        diffBuilder.getReviewerList().stream().anyMatch(Reviewer::getApproved);

    if (!hasApprovedReviews) {
      System.out.println(RED_ERROR + String.format("D%d is not approved yet", diffNumber));
      return false;
    }

    System.out.println("Updating diff status: SUBMITTING");
    codeReviewBlockingStub.createDiff(
        CreateDiffRequest.newBuilder().setDiff(diffBuilder.setStatus(Status.SUBMITTING)).build());

    final String diffBranchName = String.format("D%s", diffBuilder.getId());

    try {
      fileUtils.listContents(workspacePath).stream()
          .map(path -> fileUtils.joinToAbsolutePath(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                GitRepo repo = this.gitRepoFactory.create(path);

                boolean hasDiffBranch =
                    repo.listBranches().stream()
                        .anyMatch(branchName -> branchName.equals(diffBranchName));

                if (!hasDiffBranch) {
                  System.out.println(
                      String.format(
                          "Repo %s has no branch named %s, skipping", repoName, diffBranchName));
                  return;
                }
                System.out.println(String.format("[%s]: committing changes", repoName));
                repo.commit(
                    repo.getUncommittedFiles(),
                    String.format("%s: %s", diffBranchName, diffBuilder.getDescription()));
                System.out.println(String.format("[%s]: removing branch", repoName));
                repo.removeBranch(diffBranchName);
                System.out.println(String.format("[%s]: pushing to remote", repoName));
                repo.push(diffBranchName);
              });
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Updating diff status: SUBMITTED");
    codeReviewBlockingStub.createDiff(
        CreateDiffRequest.newBuilder().setDiff(diffBuilder.setStatus(Status.SUBMITTED)).build());
    return true;
  }
}

