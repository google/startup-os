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

package com.google.startupos.examples.docker;

import java.nio.file.Files;
import java.nio.file.Paths;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HelloServer {
  private static final String DOCKERENV_FILE = "/.dockerenv";

  static class SayHelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String response;
      if (Files.exists(Paths.get(DOCKERENV_FILE))) {
        response = String.format("Hello, I am *inside* the container! (%s exists)", DOCKERENV_FILE);
      } else {
        response =
            String.format("Hello from outside of container (%s does not exist)", DOCKERENV_FILE);
      }

      t.sendResponseHeaders(200, response.length());
      try (OutputStream os = t.getResponseBody()) {
        os.write(response.getBytes());
      }
    }
  }

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/test", new SayHelloHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }
}

