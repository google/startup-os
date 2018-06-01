package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.aa.Protos;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.Diff.Status;
import com.google.startupos.tools.reviewer.service.Protos.GetDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Reviewer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;

public class SubmitCommand implements AaCommand {

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private String workspacePath;
  private Integer currentDiffNumber;

  private static final Integer GRPC_PORT = 8001;

  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;

  @Inject
  public SubmitCommand(
      FileUtils utils,
      Protos.Config config,
      GitRepoFactory repoFactory,
      @Named("Current workspace name") String currentWorkspaceName,
      @Named("Current diff number") Integer currentDiffNumber) {
    this.fileUtils = utils;
    this.gitRepoFactory = repoFactory;
    this.workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);
    this.currentDiffNumber = currentDiffNumber;

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean run(String[] args) {
    if (currentDiffNumber == -1) {
      System.out.println(
          RED_ERROR + "Workspace has no diff to submit (git branch has no D# branch)");
      return false;
    }

    final Diff.Builder diffBuilder =
        codeReviewBlockingStub
            .getDiff(GetDiffRequest.newBuilder().setDiffId(currentDiffNumber).build())
            .toBuilder();

    boolean hasApprovedReviews =
        diffBuilder.getReviewerList().stream().anyMatch(Reviewer::getApproved);

    if (!hasApprovedReviews) {
      System.out.println(RED_ERROR + String.format("D%d is not approved yet", currentDiffNumber));
      return false;
    }

    System.out.println("Updating diff status: SUBMITTING");
    codeReviewBlockingStub.createDiff(
        CreateDiffRequest.newBuilder().setDiff(diffBuilder.setStatus(Status.SUBMITTING)).build());

    final String diffBranchName = String.format("D%s", diffBuilder.getNumber());

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

                boolean hasDiffBranch =
                    repo.listBranches()
                        .stream()
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
                repo.pushAll();
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
