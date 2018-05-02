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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.startupos.common.TextDifferencer;
import com.google.startupos.common.firestore.FirestoreClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffResponse;
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

/*
 * CodeReviewService is a gRPC service (definition in proto/code_review.proto)
 */
@AutoFactory
public class CodeReviewService extends CodeReviewServiceGrpc.CodeReviewServiceImplBase {
  private static final Logger logger = Logger.getLogger(CodeReviewService.class.getName());

  @FlagDesc(name = "firestore_review_root", description = "Review root path in Firestore")
  private static final Flag<String> firestoreReviewRoot = Flag.create("/reviewer");

  private AuthService authService;
  private String filesystemRootPath;

  public CodeReviewService(@Provided AuthService authService, String filesystemRootPath) {
    this.authService = authService;
    this.filesystemRootPath = filesystemRootPath;
  }

  private static String readTextFile(String path) throws IOException {
    return String.join(System.lineSeparator(), Files.readAllLines(Paths.get(path)));
  }

  private String getAbsolutePath(String relativePath) throws SecurityException {
    // normalize() resolves "../", to help prevent returning files outside rootPath
    String absolutePath = Paths.get(filesystemRootPath, relativePath).normalize().toString();

    if (!absolutePath.startsWith(filesystemRootPath)) {
      throw new SecurityException("Resulting path is not under root");
    }

    return absolutePath;
  }

  @Override
  public void getFile(FileRequest req, StreamObserver<FileResponse> responseObserver) {
    try {
      String filePath = getAbsolutePath(req.getFilename());
      responseObserver.onNext(FileResponse.newBuilder().setContent(readTextFile(filePath)).build());
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
      String firstFileContents = readTextFile(req.getFirstFilepath());
      String secondFileContents = readTextFile(req.getSecondFilepath());
      responseObserver.onNext(
          TextDiffResponse.newBuilder()
              .addAllChanges(
                  TextDifferencer.getAllTextChanges(firstFileContents, secondFileContents))
              .build());
    } catch (IOException e) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(String.format("No such file %s", req.getFirstFilepath()))
              .asException());
    }
    responseObserver.onCompleted();
  }
}
