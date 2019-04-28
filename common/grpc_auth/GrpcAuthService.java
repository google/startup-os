package com.google.startupos.common.grpc_auth;

import com.google.startupos.common.grpc_auth.Protos.Request;
import com.google.startupos.common.grpc_auth.Protos.Response;
import io.grpc.stub.StreamObserver;

public class GrpcAuthService extends GrpcAuthTestGrpc.GrpcAuthTestImplBase {
  @Override
  public void getNextNumber(Request request, StreamObserver<Response> responseObserver) {
    responseObserver.onNext(Response.newBuilder().setNumber(request.getNumber() + 1).build());
    responseObserver.onCompleted();
  }
}

