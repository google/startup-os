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

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.TextDifferencer;
import com.google.startupos.common.firestore.FirestoreClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffResponse;
import com.google.startupos.tools.reviewer.service.Protos.File;
import com.google.startupos.tools.reviewer.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.service.Protos.FileResponse;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.inject.Inject;

/*
 * CodeReviewService is a gRPC service (definition in proto/code_review.proto)
 */
public class CodeReviewService extends CodeReviewServiceGrpc.CodeReviewServiceImplBase {
  private static final Logger logger = Logger.getLogger(CodeReviewService.class.getName());

  @FlagDesc(name = "firestore_review_root", description = "Review root path in Firestore")
  private static final Flag<String> firestoreReviewRoot = Flag.create("/reviewer");

  private AuthService authService;
  private FileUtils fileUtils;
  private String basePath;
  private GitRepoFactory repoFactory;

  @Inject
  public CodeReviewService(AuthService authService, FileUtils fileUtils,
      @Named("Base path") String basePath, GitRepoFactory repoFactory) {
    this.authService = authService;
    this.fileUtils = fileUtils;
    this.basePath = basePath;
    this.repoFactory = repoFactory;
  }

  private String readTextFile(File file) throws IOException {
    if (file.getWorkspace().isEmpty()) {
      // It's a file in head
      String repoPath = fileUtils.joinPaths(basePath, "head", file.getRepoId());
      Repo repo = repoFactory.create(repoPath);
      return repo.getFileContents(file.getCommitId(), file.getFilename());
    } else {
      // It's a file in a workspace
      if (file.getUser().isEmpty()) {
        // It's the current user
        if (file.getCommitId().isEmpty()) {
          // It's a file in the local filesystem (not in a repo)
          String filePath = fileUtils.joinPaths(basePath, "ws", file.getWorkspace(),
              file.getRepoId(), file.getFilename());
          return fileUtils.readFile(filePath);
        } else {
          // It's a file in a repo
          String repoPath = fileUtils.joinPaths(basePath, "ws", file.getWorkspace(),
              file.getRepoId());
          Repo repo = repoFactory.create(repoPath);
          return repo.getFileContents(file.getCommitId(), file.getFilename());
        }
      } else {
        // It's another user
        if (file.getCommitId().isEmpty()) {
          // It's a file in the local filesystem (not in a repo)
          String filePath = fileUtils.joinPaths(basePath, "users", file.getUser(), "ws",
              file.getWorkspace(), file.getRepoId(), file.getFilename());
          return fileUtils.readFile(filePath);
        } else {
          // It's a file in a repo
          String repoPath = fileUtils.joinPaths(basePath, "users", file.getUser(), "ws",
              file.getWorkspace(), file.getRepoId());
          Repo repo = repoFactory.create(repoPath);
          return repo.getFileContents(file.getCommitId(), file.getFilename());
        }
      }
    }
  }

  private String getAbsolutePath(String relativePath) throws SecurityException {
    // normalize() resolves "../", to help prevent returning files outside rootPath
    String absolutePath = Paths.get(basePath, relativePath).normalize().toString();
    if (!absolutePath.startsWith(basePath)) {
      throw new SecurityException("Resulting path is not under root");
    }

    return absolutePath;
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
  public void createDiff(
      CreateDiffRequest req, StreamObserver<CreateDiffResponse> responseObserver) {
    FirestoreClient client =
        new FirestoreClient(authService.getProjectId(), authService.getToken());
    client.createDocument(firestoreReviewRoot.get(), req.getDiff());
    responseObserver.onCompleted();
  }

  @Override
  public void getTextDiff(TextDiffRequest req, StreamObserver<TextDiffResponse> responseObserver) {
    try {
      String firstFileContents = readTextFile(req.getLeftFile());
      String secondFileContents = readTextFile(req.getRightFile());
      responseObserver.onNext(
          TextDiffResponse.newBuilder()
              .addAllChanges(
                  TextDifferencer.getAllTextChanges(firstFileContents, secondFileContents))
              .build());
    } catch (IOException e) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(String.format("TextDiffRequest: %s", req))
              .asException());
    }
    responseObserver.onCompleted();
  }
}
