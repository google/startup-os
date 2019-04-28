package com.google.startupos.common.grpc_auth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class ServerAuthInterceptor implements ServerInterceptor {

  public ServerAuthInterceptor(String projectId) {
    FirebaseApp.initializeApp(
        new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.newBuilder().build())
            .setProjectId(projectId)
            .build());
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> serverCall,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> serverCallHandler) {
    String token = metadata.get(Metadata.Key.of("token", ASCII_STRING_MARSHALLER));
    System.out.println("Token: " + token);

    boolean tokenIsValid = false;

    try {
      FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
      System.err.println("Email for token: " + firebaseToken.getEmail());

      // TODO: properly validate whether user has rights
      //noinspection ConstantConditions
      if (true) {
        tokenIsValid = true;
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Was unable to parse token");
    }

    if (!tokenIsValid) {
      serverCall.close(Status.UNAUTHENTICATED, metadata);
      return new ServerCall.Listener<ReqT>() {};
    } else {
      return serverCallHandler.startCall(serverCall, metadata);
    }
  }
}

