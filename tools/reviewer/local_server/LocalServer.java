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
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.aa.AaModule;
import com.google.startupos.tools.reviewer.localserver.service.CodeReviewService;
import dagger.Component;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/*
 * LocalServer is a gRPC server (definition in proto/code_review.proto)
 *
 * To run:
 * bazel build //tools/reviewer/local_server:local_server
 * bazel-bin/tools/reviewer/local_server/local_server
 */
@Singleton
public class LocalServer {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  @FlagDesc(name = "local_server_port", description = "Port for local gRPC server")
  public static final Flag<Integer> localServerPort = Flag.create(8001);

  @FlagDesc(name = "pull_frequency", description = "Frequency of pulling head (in seconds)")
  public static final Flag<Integer> pullFrequency = Flag.create(60);

  @FlagDesc(name = "http_gateway_port", description = "Port for local HTTP gateway server")
  public static final Flag<Integer> httpGatewayPort = Flag.create(7000);

  @FlagDesc(name = "local_server_host", description = "Hostname for local gRPC server")
  public static final Flag<String> localServerHost = Flag.create("localhost");

  @FlagDesc(name = "log_to_file", description = "Log stdout and stderr to log file")
  private static final Flag<Boolean> logToFile = Flag.create(true);

  private final Server server;

  public static class HeadUpdater extends TimerTask {

    private final FileUtils fileUtils;
    private final String basePath;
    private final GitRepoFactory repoFactory;

    @Inject
    public HeadUpdater(
        FileUtils utils, @Named("Base path") String basePath, GitRepoFactory repoFactory) {
      this.fileUtils = utils;
      this.basePath = basePath;
      this.repoFactory = repoFactory;
    }

    public void run() {
      String headPath = fileUtils.joinToAbsolutePath(this.basePath, "head");

      // Pull all repos in head
      try {
        fileUtils
            .listContents(headPath)
            .stream()
            .map(path -> fileUtils.joinToAbsolutePath(headPath, path))
            .filter(fileUtils::folderExists)
            .forEach(
                path -> {
                  System.out.println(String.format("[HEAD]: Performing sync: %s", path));
                  repoFactory.create(path).pull();
                });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Inject
  LocalServer(
      @Named("Server log path") String logPath,
      AuthService authService,
      CodeReviewService codeReviewService) {
    if (logToFile.get()) {
      // TODO: Figure out how to also direct Flogger to log file.
      try {
        PrintStream logStream = new PrintStream(logPath);
        System.setOut(logStream);
        System.setErr(logStream);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    server =
        ServerBuilder.forPort(localServerPort.get())
            .addService(authService)
            .addService(codeReviewService)
            .addService(ProtoReflectionService.newInstance())
            .build();
  }

  public void start() throws IOException {
    server.start();
    logger.atInfo().log("Server started, listening on " + localServerPort.get());
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("Shutting down gRPC server since JVM is shutting down");
                  this.stop();
                  System.err.println("Server shut down");
                }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  interface LocalServerComponent {
    LocalServer getLocalServer();

    HeadUpdater getHeadUpdater();
  }

  private static void checkFlags() {
    if (httpGatewayPort.get().equals(localServerPort.get())) {
      System.out.println(
          "Error: HttpGatewayServer and LocalServer ports are the same: " + localServerPort.get());
      System.exit(1);
    }
  }

  public static void main(String[] args) throws Exception {
    Flags.parse(args, LocalServer.class.getPackage(), CodeReviewService.class.getPackage());
    checkFlags();
    LocalServerComponent component = DaggerLocalServer_LocalServerComponent.builder().build();
    LocalServer server = component.getLocalServer();
    new Timer().scheduleAtFixedRate(component.getHeadUpdater(), 0, pullFrequency.get() * 1000L);
    server.start();
    new LocalHttpGateway(httpGatewayPort.get(), localServerHost.get(), localServerPort.get())
        .serve();
    server.blockUntilShutdown();
  }
}

