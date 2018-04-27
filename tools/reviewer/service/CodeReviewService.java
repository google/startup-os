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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import com.google.startupos.tools.reviewer.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.service.Protos.FileResponse;
import com.google.startupos.tools.localserver.service.AuthService;

/*
 * CodeReviewService is a gRPC service (definition in proto/code_review.proto)
 */
public class CodeReviewService extends CodeReviewServiceGrpc.CodeReviewServiceImplBase {
  private static final Logger logger = Logger.getLogger(CodeReviewService.class.getName());
 
  private AuthService authService;
  private String rootPath;

  public CodeReviewService(AuthService authService, String rootPath) {
    this.authService = authService;
    this.rootPath = rootPath;
  }

  private static String readTextFile(String path) throws IOException {
    return String.join(System.lineSeparator(), Files.readAllLines(Paths.get(path)));
  }

  private String getAbsolutePath(String relativePath) throws SecurityException {
    // normalize() resolves "../", to help prevent returning files outside rootPath
    String absolutePath = Paths.get(this.rootPath, relativePath).normalize().toString();

    if (!absolutePath.startsWith(rootPath)) {
      throw new SecurityException("Resulting path is not under root");
    }

    return absolutePath;
  }

  @Override
  public void getFile(FileRequest req, StreamObserver<FileResponse> responseObserver) {
    try {
      String filePath = getAbsolutePath(req.getFilename());
      responseObserver.onNext(
          FileResponse.newBuilder().setContent(readTextFile(filePath)).build());
      responseObserver.onCompleted();
    } catch (SecurityException | IOException e) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription(String.format("No such file %s", req.getFilename()))
              .asException());
    }
  }
}
