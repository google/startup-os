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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;
import com.google.startupos.tools.localserver.service.Protos.AuthDataRequest;
import com.google.startupos.tools.localserver.service.Protos.AuthDataResponse;
import javax.inject.Inject;

/*
 * AuthService is a gRPC service to receive Firestore auth data from WebLogin.
 */
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {
  private static final Logger logger = Logger.getLogger(AuthService.class.getName());
 
  private String firestoreToken;
  private String firestoreProjectId;

  @Inject
  AuthService() {}

  @Override
  public void postAuthData(AuthDataRequest req, StreamObserver<AuthDataResponse> responseObserver) {
    try {
      firestoreProjectId = req.getProjectId();
      firestoreToken = req.getToken();
      responseObserver.onNext(AuthDataResponse.getDefaultInstance());
      responseObserver.onCompleted();
      logger.info("Received token for project " + firestoreProjectId);
    } catch (SecurityException e) {
      responseObserver.onError(
          Status.UNKNOWN
              .withDescription("Cannot get token from request")
              .asException());
      logger.info("Cannot get token from request");
    }
  }

  public String getToken() {
    return firestoreToken;
  }

  public String getProjectId() {
    return firestoreProjectId;
  }
}
