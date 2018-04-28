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
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Collectors;
import org.json.JSONObject;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.flags.FlagDesc;

/*
 * LocalHttpGateway is a proxy that takes HTTP calls over HTTP_GATEWAY_PORT, sends them to gRPC
 * client (which in turn communicates to gRPC server and responds) and returns responses
 *
 * To run:
 * bazel build //tools/local_server:local_http_gateway_deploy.jar
 * bazel-bin/tools/local_server/local_http_gateway -- {absolute_path}
 * {absolute_path} is absolute root path to serve files over (use `pwd` for current dir)
 */
// TODO: Find an automated way to do this, e.g github.com/improbable-eng/grpc-web
public class LocalHttpGateway {
  private static final Logger logger = Logger.getLogger(LocalHttpGateway.class.getName());

  private final HttpServer httpServer;

  private static final String GET_FILE_PATH = "/get_file";
  private static final String TOKEN_PATH = "/token";

  @FlagDesc(name = "http_gateway_port", description = "Port for local HTTP gateway server")
  public static final Flag<Integer> httpGatewayPort = Flag.create(7000);

  @FlagDesc(name = "local_server_port", description = "Port for local gRPC server")
  public static final Flag<Integer> localServerPort = Flag.create(8001);

  private LocalHttpGateway(int httpGatewayPort, int localServerPort) throws Exception {
    logger.info(String.format(
        "Starting gateway at port %d (local server at port %d)",
        httpGatewayPort,
        localServerPort));
    httpServer = HttpServer.create(new InetSocketAddress(httpGatewayPort), 0);
    LocalHttpGatewayGrpcClient client =
        new LocalHttpGatewayGrpcClient("localhost", localServerPort);

    httpServer.createContext(TOKEN_PATH, new FirestoreTokenHandler(client));
    httpServer.createContext(GET_FILE_PATH, new GetFileHandler(client));
    httpServer.setExecutor(null); // Creates a default executor
  }

  public void serve() throws Exception {
    httpServer.start();
  }

  /* Handler for receiving Firestore token */
  static class FirestoreTokenHandler implements HttpHandler {
    private LocalHttpGatewayGrpcClient client;

    FirestoreTokenHandler(LocalHttpGatewayGrpcClient client) {
      this.client = client;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
      httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers",
          "Origin, X-Requested-With, Content-Type, Accept");
      if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
        logger.info("Handling token post request");
        JSONObject json = new JSONObject(getPostParamsString(httpExchange));
        client.postAuthData(json.getString("projectId"), json.getString("accessToken"));
      }
      httpExchange.sendResponseHeaders(200, -1);
    }
  }

  /* Handler for serving /get_file/{fn} endpoint where {fn} is relative path */
  static class GetFileHandler implements HttpHandler {
    private LocalHttpGatewayGrpcClient client;

    GetFileHandler(LocalHttpGatewayGrpcClient client) {
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

  private static void checkFlags() {
    if (httpGatewayPort.get().equals(localServerPort.get())) {
      System.out.println(
          "Error: HttpGatewayServer and LocalServer ports are the same: " + localServerPort.get());
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    Flags.parse(args, LocalHttpGateway.class.getPackage());
    checkFlags();
    new LocalHttpGateway(httpGatewayPort.get(), localServerPort.get()).serve();
  }
}
