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

package com.google.startupos.tools.reviewer.remote_server;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.reviewer.remote_server.Protos.AuthResponse;
import com.google.startupos.tools.reviewer.remote_server.Protos.InitialAuthRequest;
import com.google.startupos.tools.reviewer.remote_server.Protos.RefreshTokenRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.stream.Collectors;

public class RemoteServer {
  @FlagDesc(name = "port", description = "HTTP port to run server on")
  private static final Flag<Integer> port = Flag.create(-1);

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int HTTP_STATUS_CODE_OK = 200;
  private static final int HTTP_STATUS_CODE_UNAUTHORIZED = 403;

  private static final String TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";

  private final HttpServer httpServer;

  private static String getPostParamsString(HttpExchange httpExchange) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), UTF_8))) {
      return reader.lines().collect(Collectors.joining());
    }
  }

  private static String httpPost(URL url, String data) throws Exception {
    byte[] postDataBytes = data.getBytes(UTF_8);
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
      return bufferedReader.lines().collect(Collectors.joining("\n"));
    } catch (Exception e) {
      return null;
    }
  }

  private static void sendProto(HttpExchange exchange, Message proto) throws IOException {
    byte[] responseJson = JsonFormat.printer().print(proto).getBytes();

    exchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, responseJson.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(responseJson);
    }
  }

  private static AuthResponse parseGoogleResponse(String response)
      throws InvalidProtocolBufferException {
    AuthResponse.Builder responseProto = AuthResponse.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(response, responseProto);
    return responseProto.build();
  }

  static class InitialAuthHandler implements HttpHandler {

    private String clientId;
    private String clientSecret;

    InitialAuthHandler(String clientId, String clientSecret) {
      this.clientId = clientId;
      this.clientSecret = clientSecret;
    }

    private String buildExchangeParams(String code) {
      return String.format(
          "code=%s&client_id=%s"
              + "&client_secret=%s"
              + "&redirect_uri=postmessage"
              + "&grant_type=authorization_code",
          code, this.clientId, this.clientSecret);
    }

    private AuthResponse requestTokensFromGoogle(InitialAuthRequest request) throws Exception {
      URL url = new URL(TOKEN_URL);
      String response = httpPost(url, buildExchangeParams(request.getCode()));
      return parseGoogleResponse(response);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.atInfo().log("Handling initial auth request");

        InitialAuthRequest.Builder request = InitialAuthRequest.newBuilder();
        JsonFormat.parser().merge(getPostParamsString(httpExchange), request);
        AuthResponse response = null;

        try {
          response = requestTokensFromGoogle(request.build());
        } catch (Exception e) {
          e.printStackTrace();
          httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_UNAUTHORIZED, -1);
        }

        sendProto(httpExchange, response);
      }
    }
  }

  static class RefreshTokenHandler implements HttpHandler {
    private String clientId;
    private String clientSecret;

    RefreshTokenHandler(String clientId, String clientSecret) {
      this.clientId = clientId;
      this.clientSecret = clientSecret;
    }

    private String buildRefreshParams(String refreshToken) {
      return String.format(
          "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
          this.clientId, this.clientSecret, refreshToken);
    }

    private AuthResponse tokenRefreshRequest(RefreshTokenRequest request) throws Exception {
      URL url = new URL(RemoteServer.TOKEN_URL);
      String response = httpPost(url, buildRefreshParams(request.getRefreshToken()));
      return parseGoogleResponse(response);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.atInfo().log("Handling refresh token request");

        RefreshTokenRequest.Builder req = RefreshTokenRequest.newBuilder();
        JsonFormat.parser().merge(getPostParamsString(httpExchange), req);
        AuthResponse response = null;
        try {
          response = tokenRefreshRequest(req.build());
        } catch (Exception e) {
          e.printStackTrace();
          httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_UNAUTHORIZED, -1);
        }

        sendProto(httpExchange, response);
      }
    }
  }

  private RemoteServer(String clientId, String clientSecret) throws IOException {
    httpServer =
        HttpServer.create(new InetSocketAddress(port.get()), 0);
    httpServer.createContext("/gcode", new InitialAuthHandler(clientId, clientSecret));
    httpServer.createContext("/refresh", new RefreshTokenHandler(clientId, clientSecret));
  }

  public void start() {
    this.httpServer.start();
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);

    RemoteServer server =
        new RemoteServer(
            System.getenv("REMOTESERVER_CLIENT_ID"), System.getenv("REMOTESERVER_CLIENT_SECRET"));
    server.start();
  }
}

