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

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.CommonModule;
import com.google.startupos.tools.aa.AaModule;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.service.CodeReviewService;
import dagger.Component;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/*
 * LocalServer is a gRPC server (definition in proto/code_review.proto)
 */

/* To run: bazel build //tools/local_server:local_server
 * bazel-bin/tools/local_server/local_server
 */
@Singleton
public class LocalServer {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  @FlagDesc(name = "local_server_port", description = "Port for local gRPC server")
  private static final Flag<Integer> localServerPort = Flag.create(8001);

  private Server server;

  @Inject
  LocalServer(AuthService authService, CodeReviewService codeReviewService) {
    server =
        ServerBuilder.forPort(localServerPort.get())
            .addService(authService)
            .addService(codeReviewService)
            .build();
  }

  private void start() throws IOException {
    server.start();
    logger.atInfo().log("Server started, listening on " + localServerPort.get());
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

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  public interface LocalServerComponent {
    LocalServer getLocalServer();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Flags.parse(args, LocalServer.class.getPackage(), CodeReviewService.class.getPackage());
    LocalServer server = DaggerLocalServer_LocalServerComponent.builder().build().getLocalServer();
    server.start();
    server.blockUntilShutdown();
  }
}

