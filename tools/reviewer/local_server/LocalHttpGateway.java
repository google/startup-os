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

package com.google.startupos.tools.localserver;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.tools.reviewer.localserver.service.Protos.TextDiffRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffFilesRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.stream.Collectors;
import org.json.JSONObject;

/*
 * LocalHttpGateway is a proxy that takes HTTP calls over HTTP_GATEWAY_PORT, sends them to gRPC
 * client (which in turn communicates to gRPC server and responds) and returns responses
 *
 * To run:
 * bazel build //tools/reviewer/local_server:local_http_gateway_deploy.jar
 * bazel-bin/tools/reviewer/local_server/local_http_gateway -- {absolute_path}
 * {absolute_path} is absolute root path to serve files over (use `pwd` for current dir)
 */
// TODO: Find an automated way to do this, e.g github.com/improbable-eng/grpc-web
public class LocalHttpGateway {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final HttpServer httpServer;

  private static final String TOKEN_PATH = "/token";
  private static final String GET_FILE_PATH = "/get_file";
  private static final String GET_TEXT_DIFF_PATH = "/get_text_diff";
  private static final String GET_DIFF_FILES_PATH = "/get_diff_files";
  private static final int HTTP_STATUS_CODE_OK = 200;
  private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;

  @FlagDesc(name = "http_gateway_port", description = "Port for local HTTP gateway server")
  public static final Flag<Integer> httpGatewayPort = Flag.create(7000);

  @FlagDesc(name = "local_server_port", description = "Port for local gRPC server")
  public static final Flag<Integer> localServerPort = Flag.create(8001);

  private LocalHttpGateway(int httpGatewayPort, int localServerPort) throws Exception {
    logger
        .atInfo()
        .log(
            String.format(
                "Starting gateway at port %d (local server at port %d)",
                httpGatewayPort, localServerPort));
    httpServer = HttpServer.create(new InetSocketAddress(httpGatewayPort), 0);
    LocalHttpGatewayGrpcClient client =
        new LocalHttpGatewayGrpcClient("localhost", localServerPort);

    httpServer.createContext(TOKEN_PATH, new FirestoreTokenHandler(client));
    httpServer.createContext(GET_FILE_PATH, new GetFileHandler(client));
    httpServer.createContext(GET_TEXT_DIFF_PATH, new GetTextDiffHandler(client));
    httpServer.createContext(GET_DIFF_FILES_PATH, new GetDiffFilesHandler(client));
    httpServer.setExecutor(null); // Creates a default executor
  }

  public void serve() {
    httpServer.start();
  }

  /* Handler for receiving Firestore token */
  static class FirestoreTokenHandler implements HttpHandler {
    private final LocalHttpGatewayGrpcClient client;

    FirestoreTokenHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.atInfo().log("Handling token post request");
        JSONObject json = new JSONObject(getPostParamsString(httpExchange));
        client.postAuthData(
            json.getString("projectId"),
            json.getString("apiKey"),
            json.getString("jwtToken"),
            json.getString("refreshToken"));
      }
      httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, -1);
    }
  }

  /* Handler for serving /get_file/<file path>, <file path> is a relative path */
  static class GetFileHandler implements HttpHandler {
    private final LocalHttpGatewayGrpcClient client;

    GetFileHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      String requestPath = httpExchange.getRequestURI().getPath();
      String relativeFilePath = requestPath.substring(GET_FILE_PATH.length());
      logger.atInfo().log("Handling get_file request for " + relativeFilePath);
      String fileContents = client.getFile(relativeFilePath);

      if (fileContents == null) {
        httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_NOT_FOUND, 0);
        try (OutputStream stream = httpExchange.getResponseBody()) {
          stream.write(
              String.format("{\"error\": \"No file at path '%s'\"}", relativeFilePath).getBytes());
        }
        return;
      }

      byte[] response = fileContents.getBytes();

      httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, response.length);
      try (OutputStream stream = httpExchange.getResponseBody()) {
        stream.write(response);
      }
    }
  }

  /* Handler for serving /get_text_diff?request=<protobin> where <protobin> is a request proto.
   * The response is a protobin of the response proto.
   */
  static class GetTextDiffHandler implements HttpHandler {
    private final LocalHttpGatewayGrpcClient client;

    GetTextDiffHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    private void printExampleEncodedBytes() {
      // Here's an example of a url that should work, based on the example below:
      // http://localhost:7000/get_text_diff?request=CkEKCVJFQURNRS5tZCIKc3RhcnR1cC1vcyooMTEyZGEyN2IzMjFlZDZhYTJlYzFiYzkxZjM5MThlYjQxZDhhOTM4YxJBCglSRUFETUUubWQiCnN0YXJ0dXAtb3MqKDExMmRhMjdiMzIxZWQ2YWEyZWMxYmM5MWYzOTE4ZWI0MWQ4YTkzOGM=
      File file =
          File.newBuilder()
              .setRepoId("startup-os")
              .setCommitId("112da27b321ed6aa2ec1bc91f3918eb41d8a938c")
              .setFilename("README.md")
              .build();
      final TextDiffRequest request =
          TextDiffRequest.newBuilder().setLeftFile(file).setRightFile(file).build();
      byte[] bytes = request.toByteArray();
      String encodedBytes = Base64.getEncoder().encodeToString(bytes);
      System.out.println("encodedBytes");
      System.out.println(encodedBytes);
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      // TODO: Remove printExampleEncodedBytes() once integration is working
      printExampleEncodedBytes();
      ImmutableMap<String, String> params = paramsToMap(httpExchange.getRequestURI().getQuery());
      String requestString = params.get("request");
      TextDiffRequest request =
          TextDiffRequest.parseFrom(Base64.getDecoder().decode(requestString));
      logger.atInfo().log("Handling " + GET_TEXT_DIFF_PATH + " request:\n" + request);
      byte[] response =
          Base64.getEncoder().encode(client.getCodeReviewStub().getTextDiff(request).toByteArray());
      httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, response.length);
      try (OutputStream stream = httpExchange.getResponseBody()) {
        stream.write(response);
      }
    }
  }

  /* Handler for serving /get_diff_files?request=<protobin> where <protobin> is a request proto.
   * The response is a protobin of the response proto.
   */
  static class GetDiffFilesHandler implements HttpHandler {
    private final LocalHttpGatewayGrpcClient client;

    GetDiffFilesHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    private void printExampleEncodedBytes() {
      // Here's an example of a url that should work, based on the example below:
      // http://localhost:7000/get_diff_files?request=ChxmaXhfZmlsZXNfaW5fc2VydmVyX3Jlc3BvbnNlEBw=
      final DiffFilesRequest request =
          DiffFilesRequest.newBuilder()
              .setWorkspace("fix_files_in_server_response")
              .setDiffId(28)
              .build();
      byte[] bytes = request.toByteArray();
      String encodedBytes = Base64.getEncoder().encodeToString(bytes);
      System.out.println("encodedBytes");
      System.out.println(encodedBytes);
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      // TODO: Remove printExampleEncodedBytes() once integration is working
      printExampleEncodedBytes();
      ImmutableMap<String, String> params = paramsToMap(httpExchange.getRequestURI().getQuery());
      String requestString = params.get("request");
      DiffFilesRequest request =
          DiffFilesRequest.parseFrom(Base64.getDecoder().decode(requestString));
      logger.atInfo().log("Handling " + GET_DIFF_FILES_PATH + " request:\n" + request);
      byte[] response =
          Base64.getEncoder()
              .encode(client.getCodeReviewStub().getDiffFiles(request).toByteArray());
      httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, response.length);
      try (OutputStream stream = httpExchange.getResponseBody()) {
        stream.write(response);
      }
    }
  }

  static String getPostParamsString(HttpExchange httpExchange) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), UTF_8))) {
      return reader.lines().collect(Collectors.joining());
    }
  }

  static ImmutableMap<String, String> paramsToMap(String query) {
    ImmutableMap.Builder<String, String> result = new ImmutableMap.Builder<>();
    if (query != null) {
      for (String param : query.split("&")) {
        String pair[] = param.split("=");
        if (pair.length > 1) {
          result.put(pair[0], pair[1]);
        } else {
          result.put(pair[0], "");
        }
      }
    }
    return result.build();
  }

  private static void checkFlags() {
    if (httpGatewayPort.get().equals(localServerPort.get())) {
      System.out.println(
          "Error: HttpGatewayServer and LocalServer ports are the same: " + localServerPort.get());
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    Flags.parseCurrentPackage(args);
    checkFlags();
    new LocalHttpGateway(httpGatewayPort.get(), localServerPort.get()).serve();
  }
}

