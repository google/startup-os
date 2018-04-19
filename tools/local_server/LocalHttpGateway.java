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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import java.util.Map;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.startupos.tools.reviewer.service.CodeReviewClient;
import com.google.startupos.tools.reviewer.service.Protos.GetTokenResponse;

/*
 * LocalHttpGateway is a proxy that takes HTTP calls over HTTP_GATEWAY_PORT, sends them to gRPC
 * client (which in turn communicates to gRPC server and responds) and returns responses
 *
 * To run: bazel build //tools/local_server:http_gateway_deploy.jar
 * java -jar bazel-bin/tools/local_server/http_gateway_deploy.jar -- {absolute_path}
 * {absolute_path} is absolute root path to serve files over (use `pwd` for current dir)
 */
// TODO: Find an automated way to do this, e.g github.com/improbable-eng/grpc-web
public class LocalHttpGateway {
  private static final Logger logger = Logger.getLogger(LocalHttpGateway.class.getName());

  // TODO: Receive port and root path as flags.
  public static final int HTTP_GATEWAY_PORT = 7000;

  private final HttpServer httpServer;
  private final LocalInProcessServer grpcServer;
  private String grpcServerName;

  private static final String GET_FILE_PATH = "/get_file";
  private static final String TOKEN_PATH = "/token";

  private LocalHttpGateway(int gatewayPort, String rootPath) throws Exception {
    logger.info(String.format("Starting gateway at port %d for path %s", gatewayPort, rootPath));
    grpcServerName = java.util.UUID.randomUUID().toString();
    httpServer = HttpServer.create(new InetSocketAddress(gatewayPort), 0);
    grpcServer = new LocalInProcessServer(rootPath, grpcServerName);
    CodeReviewClient client = new CodeReviewClient(grpcServerName);

    httpServer.createContext(TOKEN_PATH, new FirestoreTokenHandler(client));
    httpServer.createContext(GET_FILE_PATH, new GetFileHandler(client));
    httpServer.setExecutor(null); // creates a default executor
  }

  public void serve() throws Exception {
    httpServer.start();
    grpcServer.start();
    grpcServer.blockUntilShutdown();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      logger.severe("Please specify root path for file serving as argument");
      return;
    }
    new LocalHttpGateway(HTTP_GATEWAY_PORT, args[1]).serve();
  }

  /* Handler for receiving Firestore token */
  static class FirestoreTokenHandler implements HttpHandler {
    private CodeReviewClient client;

    FirestoreTokenHandler(CodeReviewClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers",
          "Origin, X-Requested-With, Content-Type, Accept");
      if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.info("Handling token post request");
        JSONObject json = new JSONObject(getPostParamsString(httpExchange));
        client.postToken(json.getString("projectId"), json.getString("token"));
      } else if ("get".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.info("Handling token get request");
        GetTokenResponse response = client.getToken();

        byte[] responseBytes = response.toByteArray();
        httpExchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream stream = httpExchange.getResponseBody();
        stream.write(responseBytes);
        stream.close();
      }

      httpExchange.sendResponseHeaders(200, -1);
    }
  }

  /* Handler for serving /get_file/{fn} endpoint where {fn} is relative path */
  static class GetFileHandler implements HttpHandler {
    private CodeReviewClient client;

    GetFileHandler(CodeReviewClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      String requestPath = httpExchange.getRequestURI().getPath();
      String relativeFilePath = requestPath.substring(GET_FILE_PATH.length());
      logger.info("Handling get_file request for " + relativeFilePath);
      String fileContents = client.getFile(relativeFilePath);

      if (fileContents == null) {
        httpExchange.sendResponseHeaders(404, 0);
        OutputStream stream = httpExchange.getResponseBody();
        stream.write(
            String.format("{\"error\": \"No file at path '%s'\"}", relativeFilePath).getBytes());
        stream.close();
        return;
      }

      byte[] response = fileContents.getBytes();

      httpExchange.sendResponseHeaders(200, response.length);
      OutputStream stream = httpExchange.getResponseBody();
      stream.write(response);
      stream.close();
    }
  }

  static String getPostParamsString(HttpExchange httpExchange){
    @SuppressWarnings("unchecked")
    Map<String, Object> parameters =
        (Map<String, Object>)httpExchange.getAttribute("parameters");
    BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), UTF_8));
    return reader.lines().collect(Collectors.joining());
  }

  static ImmutableMap<String, String> paramsToMap(String query){
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
