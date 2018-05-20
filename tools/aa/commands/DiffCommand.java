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
import com.google.startupos.tools.reviewer.service.Protos;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.File;
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

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 8001).usePlaintext().build();
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  @Override
  public void run(String[] args) {
    Flags.parse(args, this.getClass().getPackage());

    Protos.DiffNumberResponse response =
        codeReviewBlockingStub.getAvailableDiffNumber(Empty.getDefaultInstance());
    String branchName = String.format("D%s", response.getDiffNumber());

    Diff.Builder diffBuilder =
        Diff.newBuilder()
            .setWorkspace(currentWorkspaceName)
            .setDescription(description.get())
            .setBug(buglink.get())
            .addAllReviewer(Arrays.asList(reviewers.get().split(",")))
            .setNumber(response.getDiffNumber())
            .addAuthor(config.getUser());

    String workspacePath = fileUtils.joinPaths(config.getBasePath(), "ws", currentWorkspaceName);
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

    CreateDiffRequest request = CreateDiffRequest.newBuilder().setDiff(diffBuilder).build();
    codeReviewBlockingStub.createDiff(request);
  }
}
