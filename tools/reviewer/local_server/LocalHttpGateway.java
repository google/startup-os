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
 * LocalHttpGateway is a proxy that takes HTTP requests, sends them to gRPC client (which in turn
 * communicates to gRPC server and responds) and returns responses.
 * It is run together with the local server.
 */
public class LocalHttpGateway {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final HttpServer httpServer;

  private static final String HEALTH_CHECK_PATH = "/health";
  private static final String TOKEN_PATH = "/token";
  private static final String GET_TEXT_DIFF_PATH = "/get_text_diff";
  private static final String GET_DIFF_FILES_PATH = "/get_diff_files";
  private static final int HTTP_STATUS_CODE_OK = 200;
  private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;

  public LocalHttpGateway(int httpGatewayPort, String localServerHost, int localServerPort)
      throws Exception {
    logger.atInfo().log(String.format("Starting gateway at port %d", httpGatewayPort));
    httpServer = HttpServer.create(new InetSocketAddress(httpGatewayPort), 0);
    LocalHttpGatewayGrpcClient client =
        new LocalHttpGatewayGrpcClient(localServerHost, localServerPort);

    httpServer.createContext("/", new HealthCheckHandler());
    httpServer.createContext(HEALTH_CHECK_PATH, new HealthCheckHandler());
    httpServer.createContext(TOKEN_PATH, new FirestoreTokenHandler(client));
    httpServer.createContext(GET_TEXT_DIFF_PATH, new GetTextDiffHandler(client));
    httpServer.createContext(GET_DIFF_FILES_PATH, new GetDiffFilesHandler(client));
    httpServer.setExecutor(null); // Creates a default executor
  }

  public void serve() {
    httpServer.start();
  }

  /* Handler for health check */
  static class HealthCheckHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      final byte[] response = "OK".getBytes(UTF_8);
      httpExchange.sendResponseHeaders(HTTP_STATUS_CODE_OK, response.length);
      try (OutputStream stream = httpExchange.getResponseBody()) {
        stream.write(response);
      }
    }
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

  /* Handler for serving /get_text_diff?request=<protobin> where <protobin> is a request proto.
   * The response is a protobin of the response proto.
   */
  static class GetTextDiffHandler implements HttpHandler {
    private final LocalHttpGatewayGrpcClient client;

    GetTextDiffHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
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

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange
          .getResponseHeaders()
          .add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
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
}

