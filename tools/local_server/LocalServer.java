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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.logging.Logger;
import com.google.startupos.tools.reviewer.service.CodeReviewService;

/*
 * LocalServer is a gRPC server (definition in proto/code_review.proto)
 */

/* To run: bazel build //tools/local_server:local_server_deploy.jar
 * java -jar bazel-bin/tools/local_server/local_server_deploy.jar -- {absolute_path}
 * {absolute_path} is absolute root path to serve files over (use `pwd` for current dir)
 */
public class LocalServer {
  public static final int GRPC_PORT = 50051;

  private static final Logger logger = Logger.getLogger(LocalServer.class.getName());

  private Server server;
  private String rootPath;

  LocalServer(String rootPath) {
    this.rootPath = rootPath;
  }

  private void start() throws IOException {
    server =
        ServerBuilder.forPort(GRPC_PORT)
            .addService(new CodeReviewService(this.rootPath))
            .build()
            .start();
    logger.info("Server started, listening on " + GRPC_PORT);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("Shutting down gRPC server since JVM is shutting down");
                  LocalServer.this.stop();
                  System.err.println("Server shut down");
                }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 2) {
      logger.severe("Specify root path to serve files over as command-line argument");
      return;
    }
    final LocalServer server = new LocalServer(args[1]);
    server.start();
    server.blockUntilShutdown();
  }
}
