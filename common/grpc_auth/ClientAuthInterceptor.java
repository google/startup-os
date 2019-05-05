package com.google.startupos.common.grpc_auth;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class ClientAuthInterceptor implements ClientInterceptor {

  private String tokenValue;

  public ClientAuthInterceptor(String token) {
    tokenValue = token;
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
        channel.newCall(methodDescriptor, callOptions)) {
      @Override
      public void start(Listener<RespT> listener, Metadata metadata) {
        metadata.put(Metadata.Key.of("token", ASCII_STRING_MARSHALLER), tokenValue);
        super.start(listener, metadata);
      }
    };
  }
}

