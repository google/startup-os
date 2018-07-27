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
import com.google.startupos.tools.reviewer.service.Protos.Empty;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.DiffNumberResponse;
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

public class DiffCommand implements AaCommand {
  private final FileUtils fileUtils;
  private final GitRepoFactory gitRepoFactory;
  private String workspaceName;
  private String workspacePath;
  private Integer diffNumber;

  private static final Integer GRPC_PORT = 8001;

  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;

  @FlagDesc(name = "reviewers", description = "Reviewers (split by comma)")
  public static Flag<String> reviewers = Flag.create("");

  @FlagDesc(name = "description", description = "Description")
  public static Flag<String> description = Flag.create("");

  @FlagDesc(name = "buglink", description = "Buglink")
  public static Flag<String> buglink = Flag.create("");

  @Inject
  public DiffCommand(
      FileUtils utils,
      GitRepoFactory repoFactory,
      @Named("Workspace name") String workspaceName,
      @Named("Workspace path") String workspacePath,
      @Named("Diff number") Integer diffNumber) {
    this.fileUtils = utils;
    this.gitRepoFactory = repoFactory;
    this.workspaceName = workspaceName;
    this.workspacePath = workspacePath;
    this.diffNumber = diffNumber;

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  private ImmutableList<Reviewer> getReviewers(String reviewersInput) {
    return ImmutableList.copyOf(
        Arrays.stream(reviewersInput.split(","))
            .map(reviewer -> Reviewer.newBuilder().setEmail(reviewer.trim()).build())
            .collect(Collectors.toList()));
  }

  private Diff createDiff() {
    DiffNumberResponse response =
        codeReviewBlockingStub.getAvailableDiffNumber(Empty.getDefaultInstance());
    String branchName = String.format("D%s", response.getLastDiffId());
    System.out.println("Creating " + branchName);

    Diff.Builder diffBuilder =
        Diff.newBuilder()
            .setWorkspace(workspaceName)
            .setDescription(description.get())
            .setBug(buglink.get())
            .addAllReviewer(getReviewers(reviewers.get()))
            .setId(response.getLastDiffId());

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
                System.out.println(
                    String.format("[%s/%s]: switching to diff branch", workspaceName, repoName));
                repo.switchBranch(branchName);
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return diffBuilder.build();
  }

  /*
   * From underlying repo perspective we do the following
   * - commit all changes to a temporary branch
   * - merge temporary branch to diff branch, preferring changes in the former
   * - merge diff branch to master (without commit)
   * - update diff entry in Firestore
   * After this, there would be one extra commit on top of diff branch,
   * representing the latest changes. We cannot commit to it directly
   * because tree is not clean and branch cannot be switched with dirty tree
   */
  private Diff updateDiff(Integer diffNumber) {
    System.out.println(String.format("Updating D%d", diffNumber));

    Diff.Builder diffBuilder =
        codeReviewBlockingStub
            .getDiff(DiffRequest.newBuilder().setDiffId(diffNumber).build())
            .toBuilder();
    if (!reviewers.get().isEmpty()) {
      // adding specified reviewers
      diffBuilder.addAllReviewer(getReviewers(reviewers.get()));
    }

    if (!description.get().isEmpty()) {
      // replace description if specified
      diffBuilder.setDescription(description.get());
    }

    if (!buglink.get().isEmpty()) {
      // replace buglink if specified
      diffBuilder.setBug(buglink.get());
    }

    return diffBuilder.build();
  }

  @Override
  public boolean run(String[] args) {
    Flags.parse(args, this.getClass().getPackage());

    Diff diff = (diffNumber == -1) ? createDiff() : updateDiff(diffNumber);
    CreateDiffRequest request = CreateDiffRequest.newBuilder().setDiff(diff).build();
    codeReviewBlockingStub.createDiff(request);
    return true;
  }
}

