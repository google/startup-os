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

package com.google.startupos.tools.localserver.service;

import com.google.common.flogger.FluentLogger;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import com.google.startupos.tools.localserver.service.Protos.AuthDataRequest;
import com.google.startupos.tools.localserver.service.Protos.AuthDataResponse;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.FileUtils;

/*
 * AuthService is a gRPC service to receive Firestore auth data from WebLogin.
 */
@Singleton
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  @FlagDesc(
      name = "debug_token_mode",
      description = "Make it easy to debug by storing and reading the token from disk")
  private static final Flag<Boolean> debugTokenMode = Flag.create(false);
  private static final String DEBUGGING_TOKEN_PATH = "~/aa_token";

  private String firestoreToken;
  private String firestoreProjectId;
  private FileUtils fileUtils;

  @Inject
  AuthService(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
    if (debugTokenMode.get() && fileUtils.fileExists(DEBUGGING_TOKEN_PATH)) {
      AuthDataRequest req = (AuthDataRequest)fileUtils.readProtoBinaryUnchecked(
          DEBUGGING_TOKEN_PATH, AuthDataRequest.newBuilder());
      firestoreProjectId = req.getProjectId();
      firestoreToken = req.getToken();
      logger.atInfo().log("Loaded token from filesystem");
    }
  }

  @Override
  public void postAuthData(AuthDataRequest req, StreamObserver<AuthDataResponse> responseObserver) {
    try {
      firestoreProjectId = req.getProjectId();
      firestoreToken = req.getToken();
      responseObserver.onNext(AuthDataResponse.getDefaultInstance());
      responseObserver.onCompleted();
      logger.atInfo().log("Received token for project " + firestoreProjectId);
      if (debugTokenMode.get()) {
        fileUtils.writeProtoBinaryUnchecked(req, DEBUGGING_TOKEN_PATH);
      }
    } catch (SecurityException e) {
      responseObserver.onError(
          Status.UNKNOWN
              .withDescription("Cannot get token from request")
              .asException());
      logger.atInfo().log("Cannot get token from request");
    }
  }

  public String getToken() {
    return firestoreToken;
  }

  public String getProjectId() {
    return firestoreProjectId;
  }
}
