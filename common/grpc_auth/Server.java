package com.google.startupos.common.grpc_auth;

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import io.grpc.ServerBuilder;

import java.io.File;

public class Server {
  private static final Integer GRPC_PORT = 9999;

  @FlagDesc(
      name = "certificate_file",
      description = "SSL certificate in PEM format",
      required = true)
  public static final Flag<String> certificateFile = Flag.create("");

  @FlagDesc(name = "key_file", description = "SSL private key in PEM format", required = true)
  public static final Flag<String> keyFile = Flag.create("");

  @FlagDesc(
      name = "project_id",
      description = "Firebase project ID for token verification",
      required = true)
  public static final Flag<String> projectId = Flag.create("");

  public static void main(String[] args) throws Exception {
    Flags.parseCurrentPackage(args);

    io.grpc.Server grpcServer =
        ServerBuilder.forPort(GRPC_PORT)
            .useTransportSecurity(new File(certificateFile.get()), new File(keyFile.get()))
            .intercept(new ServerAuthInterceptor(projectId.get()))
            .addService(new GrpcAuthService())
            .build();
    grpcServer.start();
    grpcServer.awaitTermination();
  }
}

