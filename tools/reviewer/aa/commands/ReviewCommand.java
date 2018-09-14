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
import com.google.startupos.tools.reviewer.aa.Protos.Config;
import com.google.startupos.tools.reviewer.localserver.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.localserver.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff.Status;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

/* A command to either start a review, or send back fixes.
 *
 * Snapshots the workspace (comitting all changes), and pushes all repos.
 * Sets the Attention to reviewers, removes it from author, and set status to UNDER_REVIEW.
 */
// TODO: Add the "Snapshots the workspace (comitting all changes), and pushes all repos." part.
public class ReviewCommand implements AaCommand {
  private static final Integer GRPC_PORT = 8001;

  private final FileUtils fileUtils;
  private final GitRepoFactory gitRepoFactory;
  private String workspacePath;

  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;
  private final Integer diffNumber;

  @Inject
  public ReviewCommand(
      FileUtils utils,
      Config config,
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
          RED_ERROR + "Workspace has no diff to review (git branch has no D# branch)");
      return false;
    }
    String branchName = String.format("D%d", diffNumber);
    Diff.Builder diffBuilder =
        codeReviewBlockingStub
            .getDiff(DiffRequest.newBuilder().setDiffId(diffNumber).build())
            .toBuilder();
    if (diffBuilder.getReviewerCount() == 0) {
      System.out.println(String.format("D%d has no reviewers", diffNumber));
      return false;
    }
    // TODO: Fail if SUBMITTING, SUBMITTED, REVERTING or REVERTED.
    for (int i = 0; i < diffBuilder.getReviewerCount(); i++) {
      diffBuilder.setReviewer(i, diffBuilder.getReviewer(i).toBuilder().setNeedsAttention(true));
    }
    diffBuilder.setAuthor(diffBuilder.getAuthor().toBuilder().setNeedsAttention(false));
    diffBuilder.setStatus(Status.UNDER_REVIEW).build();

    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinToAbsolutePath(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                GitRepo repo = this.gitRepoFactory.create(path);
                repo.push(branchName);
              });
    } catch (IOException e) {
      e.printStackTrace();
    }

    codeReviewBlockingStub.createDiff(
        CreateDiffRequest.newBuilder().setDiff(diffBuilder.build()).build());

    return true;
  }
}

