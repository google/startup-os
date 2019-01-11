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
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.tools.localserver.service.Protos.AuthDataRequest;
import com.google.startupos.tools.localserver.service.Protos.AuthDataResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;

/*
 * AuthService is a gRPC service to receive Firestore auth data from WebLogin.
 */
@Singleton
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final String REFRESH_TOKEN = "https://securetoken.googleapis.com/v1/token?key=%s";

  @FlagDesc(name = "disk_token_mode", description = "Read and write token to disk")
  private static final Flag<Boolean> diskTokenMode = Flag.create(true);

  private static final String DEBUGGING_TOKEN_PATH = "~/aa_token";

  private final FileUtils fileUtils;
  private final ScheduledExecutorService tokenRefreshScheduler =
      Executors.newScheduledThreadPool(1);
  private String projectId;
  private String apiKey;
  private String jwtToken;
  // Expiration time in seconds
  private long tokenExpiration;
  private String refreshToken;
  private String userName;
  private String userEmail;

  @Inject
  AuthService(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
    if (diskTokenMode.get() && fileUtils.fileExists(DEBUGGING_TOKEN_PATH)) {
      AuthDataRequest req =
          (AuthDataRequest)
              fileUtils.readProtoBinaryUnchecked(
                  DEBUGGING_TOKEN_PATH, AuthDataRequest.newBuilder());
      projectId = req.getProjectId();
      apiKey = req.getApiKey();
      jwtToken = req.getJwtToken();
      refreshToken = req.getRefreshToken();
      decodeJwtToken();
      setTokenRefreshScheduler();
      logger.atInfo().log("Loaded token from filesystem");
      refreshToken();
    }
  }

  @Override
  public void postAuthData(AuthDataRequest req, StreamObserver<AuthDataResponse> responseObserver) {
    try {
      projectId = req.getProjectId();
      apiKey = req.getApiKey();
      jwtToken = req.getJwtToken();
      refreshToken = req.getRefreshToken();
      decodeJwtToken();
      setTokenRefreshScheduler();
      responseObserver.onNext(AuthDataResponse.getDefaultInstance());
      responseObserver.onCompleted();
      logger.atInfo().log("Received token for project " + projectId);
      if (diskTokenMode.get()) {
        fileUtils.writeProtoBinaryUnchecked(req, DEBUGGING_TOKEN_PATH);
      }
    } catch (SecurityException e) {
      responseObserver.onError(
          Status.UNKNOWN.withDescription("Cannot get token from request").asException());
      logger.atInfo().log("Cannot get token from request");
    }
  }

  private void setTokenRefreshScheduler() {
    // Wait until 10 seconds before token expiration to refresh.
    long delay = tokenExpiration - (System.currentTimeMillis() / 1000) - 10;
    tokenRefreshScheduler.schedule(
        () -> {
          try {
            refreshToken();
            setTokenRefreshScheduler();
          } catch (RuntimeException e) {
            e.printStackTrace();
          }
        },
        delay,
        TimeUnit.SECONDS);
  }

  // Sets some fields such as userName, userEmail from the token.
  private void decodeJwtToken() {
    String[] parts = jwtToken.split("[.]");
    try {
      if (parts.length < 2) {
        throw new IllegalStateException("Expected 2 or more parts in token, found " + parts.length);
      }
      JSONObject json =
          new JSONObject(
              new String(Base64.getUrlDecoder().decode(parts[1].getBytes("UTF-8")), "UTF-8"));
      userName = json.getString("name");
      userEmail = json.getString("email");
      tokenExpiration = json.getLong("exp");
    } catch (Exception e) {
      throw new RuntimeException("Cannot decode JWT token", e);
    }
  }

  public void refreshToken() {
    try {
      URL url = new URL(String.format(REFRESH_TOKEN, apiKey));
      String data = "grant_type=refresh_token&refresh_token=" + refreshToken;
      byte[] postDataBytes = data.getBytes("UTF-8");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
      connection.setDoOutput(true);
      connection.getOutputStream().write(postDataBytes);

      try (InputStream stream =
              (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                  ? connection.getInputStream()
                  : connection.getErrorStream();
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream))) {

        String response = bufferedReader.lines().collect(Collectors.joining("\n"));
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
          JSONObject json = new JSONObject(response);
          jwtToken = json.getString("access_token");
          decodeJwtToken();
          logger.atInfo().log("Token refreshed. New expiration is %d", tokenExpiration);
        } else {
          throw new RuntimeException("Error on token refresh:\n" + response);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getToken() {
    return jwtToken;
  }

  public long getTokenExpiration() {
    return tokenExpiration;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}

