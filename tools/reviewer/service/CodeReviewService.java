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

package com.google.startupos.tools.reviewer.service;

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.firestore.FirestoreClientFactory;
import com.google.startupos.tools.reviewer.service.Protos.Empty;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.TextDifferencer;
import com.google.startupos.common.firestore.FirestoreClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.service.Protos.Author;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;
import com.google.startupos.tools.reviewer.service.Protos.DiffNumberResponse;
import com.google.startupos.common.repo.Protos.File.Action;
import com.google.startupos.tools.reviewer.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.service.Protos.FileResponse;
import com.google.startupos.tools.reviewer.service.Protos.DiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.DiffFilesRequest;
import com.google.startupos.tools.reviewer.service.Protos.DiffFilesResponse;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffResponse;
import com.google.startupos.tools.reviewer.service.Protos.PongResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.BranchInfo;
import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos.File;

/*
 * CodeReviewService is a gRPC service (definition in proto/code_review.proto)
 */
@Singleton
public class CodeReviewService extends CodeReviewServiceGrpc.CodeReviewServiceImplBase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  @FlagDesc(name = "firestore_review_root", description = "Review root path in Firestore")
  private static final Flag<String> firestoreReviewRoot = Flag.create("/reviewer");

  private static final String DOCUMENT_FOR_LAST_DIFF_NUMBER = "data";

  private final AuthService authService;
  private final FileUtils fileUtils;
  private final GitRepoFactory repoFactory;
  private final String basePath;
  private final TextDifferencer textDifferencer;
  private final FirestoreClientFactory firestoreClientFactory;

  @Inject
  public CodeReviewService(
      AuthService authService,
      FileUtils fileUtils,
      @Named("Base path") String basePath,
      GitRepoFactory repoFactory,
      TextDifferencer textDifferencer,
      FirestoreClientFactory firestoreClientFactory) {
    this.authService = authService;
    this.fileUtils = fileUtils;
    this.basePath = basePath;
    this.repoFactory = repoFactory;
    this.textDifferencer = textDifferencer;
    this.firestoreClientFactory = firestoreClientFactory;
  }

  private Repo getHeadRepo(String repoId) {
    String repoPath = fileUtils.joinPaths(basePath, "head", repoId);
    return repoFactory.create(repoPath);
  }

  private String readTextFile(File file) throws IOException {
    if (file.getFilename().isEmpty()) {
      throw new IllegalArgumentException("File does not have a filename:\n" + file);
    }
    if (file.getAction() == Action.DELETE) {
      return "";
    }
    try {
      if (file.getWorkspace().isEmpty()) {
        // It's a file in head
        return getHeadRepo(file.getRepoId())
            .getFileContents(file.getCommitId(), file.getFilename());
      } else {
        // It's a file in a workspace
        if (file.getUser().isEmpty()) {
          // It's the current user
          if (file.getCommitId().isEmpty()) {
            // It's a file in the local filesystem (not in a repo)
            String filePath =
                fileUtils.joinPaths(
                    basePath, "ws", file.getWorkspace(), file.getRepoId(), file.getFilename());
            return fileUtils.readFile(filePath);
          } else {
            // It's a file in a repo
            if (workspaceExists(file.getWorkspace())) {
              String repoPath =
                  fileUtils.joinPaths(basePath, "ws", file.getWorkspace(), file.getRepoId());
              Repo repo = repoFactory.create(repoPath);
              return repo.getFileContents(file.getCommitId(), file.getFilename());
            } else {
              // Workspace doesn't exist, try to read from head repo
              if (!getHeadRepo(file.getRepoId()).commitExists(file.getCommitId())) {
                logger
                    .atInfo()
                    .log(
                        "Workspace %s doesn't exist, and commit %s doesn't exist in head repo",
                        file.getWorkspace(), file.getCommitId());
                return "";
              }
              file = file.toBuilder().clearWorkspace().build();
              return readTextFile(file);
            }
          }
        } else {
          // It's another user
          if (file.getCommitId().isEmpty()) {
            // It's a file in the local filesystem (not in a repo)
            String filePath =
                fileUtils.joinPaths(
                    basePath,
                    "users",
                    file.getUser(),
                    "ws",
                    file.getWorkspace(),
                    file.getRepoId(),
                    file.getFilename());
            return fileUtils.readFile(filePath);
          } else {
            // It's a file in a repo
            String repoPath =
                fileUtils.joinPaths(
                    basePath, "users", file.getUser(), "ws", file.getWorkspace(), file.getRepoId());
            Repo repo = repoFactory.create(repoPath);
            return repo.getFileContents(file.getCommitId(), file.getFilename());
          }
        }
      }
    } catch (RuntimeException e) {
      logger.atSevere().withCause(e).log("readTextFile failed for:\n" + file);
      return "";
    }
  }

  @Override
  public void getFile(FileRequest req, StreamObserver<FileResponse> responseObserver) {
    try {
      String filePath = fileUtils.joinPaths(basePath, "head", "startup-os", req.getFilename());
      responseObserver.onNext(
          FileResponse.newBuilder().setContent(fileUtils.readFile(filePath)).build());
      responseObserver.onCompleted();
    } catch (SecurityException | IOException e) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(String.format("No such file %s", req.getFilename()))
              .asException());
    }
  }

  @Override
  public void createDiff(CreateDiffRequest req, StreamObserver<Empty> responseObserver) {
    FirestoreClient client =
        firestoreClientFactory.create(authService.getProjectId(), authService.getToken());
    String diffPath = fileUtils.joinPaths(firestoreReviewRoot.get(), "data/diff");
    Diff diff =
        req.getDiff()
            .toBuilder()
            .setAuthor(Author.newBuilder().setEmail(authService.getUserEmail()).build())
            .build();
    client.createProtoDocument(diffPath, String.valueOf(diff.getId()), diff);
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void getTextDiff(TextDiffRequest req, StreamObserver<TextDiffResponse> responseObserver) {
    try {
      String leftFileContents = readTextFile(req.getLeftFile());
      String rightFileContents = readTextFile(req.getRightFile());
      responseObserver.onNext(
          TextDiffResponse.newBuilder()
              .setTextDiff(textDifferencer.getTextDiff(leftFileContents, rightFileContents))
              .build());
    } catch (IOException e) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(String.format("TextDiffRequest: %s", req))
              .asException());
    }
    responseObserver.onCompleted();
  }

  // TODO: fix concurrency issues (if two different call method at same time)
  // could be done by wrapping in a transaction
  @Override
  public void getAvailableDiffNumber(
      Empty request, StreamObserver<DiffNumberResponse> responseObserver) {
    FirestoreClient client =
        firestoreClientFactory.create(authService.getProjectId(), authService.getToken());
    DiffNumberResponse diffNumberResponse =
        (DiffNumberResponse)
            client.getDocument(
                firestoreReviewRoot.get() + "/" + DOCUMENT_FOR_LAST_DIFF_NUMBER,
                DiffNumberResponse.newBuilder());
    diffNumberResponse =
        diffNumberResponse
            .toBuilder()
            .setLastDiffId(diffNumberResponse.getLastDiffId() + 1)
            .build();
    client.createDocument(
        firestoreReviewRoot.get(), DOCUMENT_FOR_LAST_DIFF_NUMBER, diffNumberResponse);
    responseObserver.onNext(diffNumberResponse);
    responseObserver.onCompleted();
  }

  @Override
  public void getDiff(DiffRequest request, StreamObserver<Protos.Diff> responseObserver) {
    FirestoreClient client =
        firestoreClientFactory.create(authService.getProjectId(), authService.getToken());
    String diffPath =
        fileUtils.joinPaths(
            firestoreReviewRoot.get(), "data/diff", String.valueOf(request.getDiffId()));
    Diff diff = (Diff) client.getProtoDocument(diffPath, Diff.newBuilder());

    responseObserver.onNext(diff);
    responseObserver.onCompleted();
  }

  private ImmutableList<File> addWorkspaceAndRepoToFiles(
      List<File> files, String workspace, String repoId) {
    return ImmutableList.copyOf(
        files
            .stream()
            .map(file -> file.toBuilder().setWorkspace(workspace).setRepoId(repoId).build())
            .collect(Collectors.toList()));
  }

  private ImmutableList<Commit> addWorkspaceAndRepoToCommits(
      ImmutableList<Commit> commits, String workspace, String repoId) {
    ImmutableList.Builder<Commit> result = ImmutableList.builder();
    for (Commit commit : commits) {
      ImmutableList<File> files =
          addWorkspaceAndRepoToFiles(commit.getFileList(), workspace, repoId);
      commit = commit.toBuilder().clearFile().addAllFile(files).build();
      result.add(commit);
    }
    return result.build();
  }

  String getWorkspacePath(String workspace) {
    return fileUtils.joinPaths(basePath, "ws", workspace);
  }

  boolean workspaceExists(String workspace) {
    return fileUtils.folderExists(getWorkspacePath(workspace));
  }

  @Override
  public void getDiffFiles(
      DiffFilesRequest request, StreamObserver<DiffFilesResponse> responseObserver) {
    if (request.getWorkspace().isEmpty() || request.getDiffId() <= 0) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription(
                  "Workspace must be set and diff_id must be > 0.\nrequest:\n" + request)
              .asRuntimeException());
      return;
    }
    String workspacePath;
    String workspace;
    String branch;
    boolean isHead;
    if (workspaceExists(request.getWorkspace())) {
      workspacePath = getWorkspacePath(request.getWorkspace());
      workspace = request.getWorkspace();
      branch = "D" + request.getDiffId();
      isHead = false;
    } else {
      logger
          .atInfo()
          .log("Workspace %s does not exist. Fallbacking to head.", request.getWorkspace());
      workspacePath = fileUtils.joinPaths(basePath, "head");
      workspace = "";
      branch = "remotes/origin/D" + request.getDiffId();
      isHead = true;
    }

    DiffFilesResponse.Builder response = DiffFilesResponse.newBuilder();
    try {
      fileUtils
          .listContents(workspacePath)
          .stream()
          .map(path -> fileUtils.joinPaths(workspacePath, path))
          .filter(fileUtils::folderExists)
          .forEach(
              path -> {
                String repoName = Paths.get(path).getFileName().toString();
                Repo repo = repoFactory.create(path);
                if (!isHead) {
                  ImmutableList<Commit> commits =
                      addWorkspaceAndRepoToCommits(repo.getCommits(branch), workspace, repoName);
                  ImmutableList<File> uncommittedFiles =
                      addWorkspaceAndRepoToFiles(repo.getUncommittedFiles(), workspace, repoName);
                  response.addBranchInfo(
                      BranchInfo.newBuilder()
                          .setDiffId(request.getDiffId())
                          .setRepoId(repoName)
                          .addAllCommit(commits)
                          .addAllUncommittedFile(uncommittedFiles)
                          .build());
                } else {
                  ImmutableList<Commit> commits = ImmutableList.of();
                  if (repo.branchExists(branch)) {
                    commits =
                        addWorkspaceAndRepoToCommits(repo.getCommits(branch), workspace, repoName);
                  } else {
                    logger.atInfo().log("Branch %s does not exist in %s head", branch, workspace);
                  }
                  response.addBranchInfo(
                      BranchInfo.newBuilder()
                          .setDiffId(request.getDiffId())
                          .setRepoId(repoName)
                          .addAllCommit(commits)
                          .build());
                }
              });
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.atInfo().log("DiffFiles request\n%s", request);
    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }

  @Override
  public void ping(Empty req, StreamObserver<PongResponse> responseObserver) {
    responseObserver.onNext(PongResponse.newBuilder().setMessage("pong").build());
    responseObserver.onCompleted();
  }
}

