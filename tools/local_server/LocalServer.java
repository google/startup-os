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
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.service.CodeReviewService;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.flags.FlagDesc;

/*
 * LocalServer is a gRPC server (definition in proto/code_review.proto)
 */

/* To run: bazel build //tools/local_server:local_server_deploy.jar
 * bazel-bin/tools/local_server/local_server -- {absolute_path}
 * {absolute_path} is absolute root path to serve files over (use `pwd` for current dir)
 */
public class LocalServer {
  private static final Logger logger = Logger.getLogger(LocalServer.class.getName());

  @FlagDesc(name = "local_server_port", description = "Port for local gRPC server")
  private static final Flag<Integer> localServerPort = Flag.create(8001);

  @FlagDesc(name = "root_path", description = "Root path for serving files for reviewer service")
  private static final Flag<String> rootPath = Flag.create("");

  private Server server;

  private void start() throws IOException {
    AuthService authService = new AuthService();
    server =
        ServerBuilder.forPort(localServerPort.get())
            .addService(authService)
            .addService(new CodeReviewService(authService, rootPath.get()))
            .build()
            .start();
    logger.info("Server started, listening on " + localServerPort.get());
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

  private static void checkFlags() {
    if (rootPath.get().isEmpty()) {
        System.out.println("Error: Please set --root_path");
        System.exit(1);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Flags.parse(args, LocalServer.class.getPackage(), CodeReviewService.class.getPackage());
    checkFlags();

    final LocalServer server = new LocalServer();
    server.start();
    server.blockUntilShutdown();
  }
}
