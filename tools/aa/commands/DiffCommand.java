package com.google.startupos.tools.aa.commands;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Empty;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.tools.aa.Protos.Config;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.DiffNumberResponse;
import com.google.startupos.tools.reviewer.service.Protos.File;
import com.google.startupos.tools.reviewer.service.Protos.GetDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.SingleRepoSnapshot;
import com.google.startupos.tools.reviewer.service.Protos.Snapshot;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Named;

public class DiffCommand implements AaCommand {
  private FileUtils fileUtils;
  private Config config;
  private GitRepoFactory gitRepoFactory;
  private String currentWorkspaceName;
  private String workspacePath;

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
      Config config,
      GitRepoFactory repoFactory,
      @Named("Current workspace name") String currentWorkspaceName) {
    this.fileUtils = utils;
    this.config = config;
    this.gitRepoFactory = repoFactory;
    this.currentWorkspaceName = currentWorkspaceName;
    this.workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  private Diff createDiff() {
    System.out.println("mode: creating diff");
    DiffNumberResponse response =
        codeReviewBlockingStub.getAvailableDiffNumber(Empty.getDefaultInstance());
    String branchName = String.format("D%s", response.getLastDiffId());

    Snapshot.Builder snapshotBuilder = Snapshot.newBuilder();

    Diff.Builder diffBuilder =
        Diff.newBuilder()
            .setWorkspace(currentWorkspaceName)
            .setDescription(description.get())
            .setBug(buglink.get())
            .addAllReviewer(Arrays.asList(reviewers.get().split(",")))
            .setNumber(response.getLastDiffId())
            .addAuthor(config.getUser());

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
                System.out.println(
                    String.format(
                        "[%s/%s]: switching to diff branch", currentWorkspaceName, repoName));
                repo.switchBranch(branchName);
                ImmutableList<File> files = repo.getUncommittedFiles();
                System.out.println(
                    String.format("[%s/%s]: committing changes", currentWorkspaceName, repoName));
                Commit commit = repo.commit(files, String.format("Changes done in %s", branchName));
                diffBuilder.addAllFile(commit.getFileList());

                snapshotBuilder.addSnapshot(
                    SingleRepoSnapshot.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setRepoId(repoName)
                        .setCommitId(commit.getId())
                        .setAuthor(config.getUser())
                        .setForReview(false)
                        .addAllFile(commit.getFileList())
                        .build());

                System.out.println(
                    String.format("[%s/%s]: switching to master", currentWorkspaceName, repoName));
                repo.switchBranch("master");
                System.out.println(
                    String.format("[%s/%s]: merging changes", currentWorkspaceName, repoName));
                repo.merge(branchName);
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    diffBuilder.addSnapshot(snapshotBuilder);
    return diffBuilder.build();
  }

  private Diff updateDiff(Integer diffNumber) {
    System.out.println(String.format("mode: updating diff %d", diffNumber));

    String branchName = String.format("D%d", diffNumber);

    Diff.Builder diffBuilder =
        codeReviewBlockingStub
            .getDiff(GetDiffRequest.newBuilder().setDiffId(diffNumber).build())
            .toBuilder();
    if (!reviewers.get().isEmpty()) {
      // adding specified reviewers
      diffBuilder.addAllReviewer(Arrays.asList(reviewers.get().split(",")));
    }

    if (!description.get().isEmpty()) {
      // replace description if specified
      diffBuilder.setDescription(description.get());
    }

    if (!buglink.get().isEmpty()) {
      // replace buglink if specified
      diffBuilder.setBug(buglink.get());
    }

    Snapshot.Builder snapshotBuilder = Snapshot.newBuilder();

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
                System.out.println(
                    String.format(
                        "[%s/%s]: switching to temporary branch", currentWorkspaceName, repoName));

                repo.switchBranch("_temporary");

                ImmutableList<File> files = repo.getUncommittedFiles();
                System.out.println(
                    String.format("[%s/%s]: committing changes", currentWorkspaceName, repoName));

                Commit commit = repo.commit(files, String.format("Changes done in %s", branchName));
                diffBuilder.addAllFile(commit.getFileList());

                snapshotBuilder.addSnapshot(
                    SingleRepoSnapshot.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setRepoId(repoName)
                        .setCommitId(commit.getId())
                        .setAuthor(config.getUser())
                        .setForReview(false)
                        .addAllFile(commit.getFileList())
                        .build());

                System.out.println(
                    String.format(
                        "[%s/%s]: switching to diff branch", currentWorkspaceName, repoName));
                repo.switchBranch(branchName);

                System.out.println(
                    String.format(
                        "[%s/%s]: merging temporary branch", currentWorkspaceName, repoName));
                repo.mergeTheirs("_temporary");

                System.out.println(
                    String.format(
                        "[%s/%s]: removing temporary branch", currentWorkspaceName, repoName));
                repo.removeBranch("_temporary");

                System.out.println(
                    String.format("[%s/%s]: switching to master", currentWorkspaceName, repoName));
                repo.switchBranch("master");

                System.out.println(
                    String.format("[%s/%s]: merging changes", currentWorkspaceName, repoName));
                repo.merge(branchName);
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    diffBuilder.addSnapshot(snapshotBuilder);
    return diffBuilder.build();
  }

  @Override
  public void run(String[] args) {
    Flags.parse(args, this.getClass().getPackage());

    try {
      String firstWorkspacePath =
          fileUtils
              .listContents(workspacePath)
              .stream()
              .map(path -> fileUtils.joinPaths(workspacePath, path))
              .filter(path -> fileUtils.folderExists(path))
              .findFirst()
              .orElse(null);

      if (firstWorkspacePath == null) {
        throw new RuntimeException(
            String.format("There are no repositories in workspace %s", workspacePath));
      }

      GitRepo repo = this.gitRepoFactory.create(firstWorkspacePath);
      int diffNumber =
          repo.listBranches()
              .stream()
              .filter(branchName -> branchName.startsWith("D"))
              .mapToInt(branchName -> Integer.parseInt(branchName.replace("D", "")))
              .findFirst()
              .orElse(-1);

      Diff diff = (diffNumber == -1) ? createDiff() : updateDiff(diffNumber);
      CreateDiffRequest request = CreateDiffRequest.newBuilder().setDiff(diff).build();
      codeReviewBlockingStub.createDiff(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
