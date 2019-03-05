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

package com.google.startupos.tools.reviewer.cas_proxy;

import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.tools.reviewer.proxy.AuthProtos.FileDownloadRequest;
import com.google.startupos.tools.reviewer.proxy.AuthProtos.FileDownloadResponse;
import com.google.startupos.tools.reviewer.proxy.AuthProtos.FileUploadRequest;
import com.google.startupos.tools.reviewer.proxy.AuthProtos.FileUploadResponse;
import com.google.startupos.tools.reviewer.proxy.CASProxyServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Proxy {

  // TODO(vmax): make bucket configurable
  public static final String CAS_BUCKET_NAME = "test-bucket-cas";

  static class AccessDeniedException extends Exception {}

  static class Service extends CASProxyServiceGrpc.CASProxyServiceImplBase {
    private FirestoreProtoClient client;
    private AccessManager accessManager;
    private HashMap<String, String> fileCache = new HashMap<>();

    static class FileUploader implements StreamObserver<FileUploadRequest> {

      private StreamObserver<FileUploadResponse> responseObserver;
      private FirestoreProtoClient protoClient;
      private ByteArrayOutputStream buffer;
      private AccessManager accessManager;
      private AtomicBoolean hasAccess = new AtomicBoolean(false);

      public FileUploader(
          FirestoreProtoClient protoClient,
          AccessManager accessManager,
          StreamObserver<FileUploadResponse> responseObserver) {
        this.responseObserver = responseObserver;
        this.protoClient = protoClient;
        this.buffer = new ByteArrayOutputStream();
        this.accessManager = accessManager;
      }

      @Override
      public void onNext(FileUploadRequest value) {
        if (this.accessManager.hasWriteAccess(value.getIdToken())) {
          hasAccess.set(true);
        }

        if (!hasAccess.get()) {
          responseObserver.onError(new AccessDeniedException());
        }

        try {
          buffer.write(value.getContent().toByteArray());
        } catch (IOException e) {
          e.printStackTrace();
          responseObserver.onError(e);
        }
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        String sha256 = Hashing.sha256().hashBytes(buffer.toByteArray()).toString();
        File tempFile;
        try {
          tempFile = File.createTempFile(sha256, ".tmp");
          FileOutputStream outputStream = new FileOutputStream(tempFile);
          outputStream.write(buffer.toByteArray());
          outputStream.close();

          protoClient.uploadTo(Proxy.CAS_BUCKET_NAME, tempFile.toPath().toString(), sha256);
          responseObserver.onNext(FileUploadResponse.newBuilder().setSha256(sha256).build());
        } catch (IOException e) {
          e.printStackTrace();
          responseObserver.onError(e);
        }
      }
    }

    @Override
    public void downloadFile(
        FileDownloadRequest request, StreamObserver<FileDownloadResponse> responseObserver) {

      if (!this.accessManager.hasReadAccess(request.getIdToken(), request.getSha256())) {
        responseObserver.onError(new AccessDeniedException());
      }

      String fileName =
          fileCache.computeIfAbsent(
              request.getSha256(),
              (sha256) -> {
                try {
                  return client.downloadFrom(CAS_BUCKET_NAME, sha256);
                } catch (IOException e) {
                  responseObserver.onError(e);
                  return null;
                }
              });

      if (fileName == null) {
        return;
      }

      try {
        File file = new File(fileName);
        FileInputStream stream = new FileInputStream(file);

        byte[] chunk = new byte[1024];

        int bytesRead;

        while ((bytesRead = stream.read(chunk)) != -1) {
          responseObserver.onNext(
              FileDownloadResponse.newBuilder()
                  .setContent(ByteString.copyFrom(chunk, 0, bytesRead))
                  .build());
        }

        responseObserver.onCompleted();
      } catch (IOException e) {
        e.printStackTrace();
        responseObserver.onError(e);
      }
    }

    @Override
    public StreamObserver<FileUploadRequest> uploadFile(
        StreamObserver<FileUploadResponse> responseObserver) {
      return new FileUploader(this.client, this.accessManager, responseObserver);
    }

    public Service(String serviceAccountPath, AccessManager accessManager) {
      this.client = new FirestoreProtoClient(serviceAccountPath);
      if (accessManager == null) {
        this.accessManager = new PublicAccess();
      } else {
        this.accessManager = accessManager;
      }
    }

    public Service(String serviceAccountPath) {
      this(serviceAccountPath, null);
    }
  }

  public static void main(String[] args) throws Exception {

    // TODO(vmax): replace with //common/flags cmdline arg parsing
    // TODO(vmax): make port configurable

    String serviceAccountJson = args[0];

    Server grpcServer =
        ServerBuilder.forPort(6000).addService(new Service(serviceAccountJson)).build();
    grpcServer.start();
    grpcServer.awaitTermination();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("Shutting down gRPC server since JVM is shutting down");
                  grpcServer.shutdown();
                  System.err.println("Server shut down");
                }));
  }
}

