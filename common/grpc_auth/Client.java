package com.google.startupos.common.grpc_auth;

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.grpc.netty.NettyChannelBuilder;

import java.io.File;
import java.util.logging.Level;

public class Client {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final Integer GRPC_PORT = 9999;

  @FlagDesc(
      name = "certificate_file",
      description = "SSL certificate in PEM format",
      required = true)
  public static final Flag<String> certificateFile = Flag.create("");

  @FlagDesc(name = "token", description = "Firebase ID token to authenticate", required = true)
  public static final Flag<String> token = Flag.create("");

  @FlagDesc(name = "n", description = "Argument for gRPC call", required = true)
  public static final Flag<Integer> n = Flag.create(-1);

  public static void main(String[] args) throws Exception {
    Flags.parseCurrentPackage(args);

    SslContext sslContext =
        GrpcSslContexts.forClient().trustManager(new File(certificateFile.get())).build();
    ManagedChannel channel =
        NettyChannelBuilder.forAddress("localhost", GRPC_PORT).sslContext(sslContext).build();

    GrpcAuthTestGrpc.GrpcAuthTestBlockingStub stub =
        GrpcAuthTestGrpc.newBlockingStub(channel)
            .withInterceptors(new ClientAuthInterceptor(token.get()));

    logger.at(Level.INFO).log("Calling server to increment %d", n.get());
    Protos.Response resp =
        stub.getNextNumber(Protos.Request.newBuilder().setNumber(n.get()).build());
    logger.at(Level.INFO).log("Got %d in response", resp.getNumber());
  }
}

