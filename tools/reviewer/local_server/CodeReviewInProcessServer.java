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

package com.google.startupos.tools.reviewer.localserver;

import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.util.logging.Logger;

/*
 * CodeReviewInprocessServer is a gRPC server, wrapped by CodeReviewGateway to
 * provide an HTTP endpoint.
 */
public class CodeReviewInProcessServer {
  private static final Logger logger = Logger.getLogger(CodeReviewInProcessServer.class.getName());

  private Server server;
  private String rootPath;
  private String serverName;

  CodeReviewInProcessServer(String rootPath, String serverName) {
    this.rootPath = rootPath;
    this.serverName = serverName;
  }

  public void start() throws IOException {
    server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor() // directExecutor is fine for unit tests
            .addService(new CodeReviewService(rootPath))
            .build()
            .start();

    logger.info("In-process server started, name=" + serverName);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("Shutting down gRPC server since JVM is shutting down");
                  stop();
                  System.err.println("Server shut down");
                }));
  }

  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
