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

import com.google.protobuf.ByteString;
import com.google.startupos.tools.reviewer.proxy.AuthProtos;
import com.google.startupos.tools.reviewer.proxy.CASProxyServiceGrpc;
import com.google.startupos.tools.reviewer.proxy.CASProxyServiceGrpc.CASProxyServiceBlockingStub;
import com.google.startupos.tools.reviewer.proxy.CASProxyServiceGrpc.CASProxyServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.Executors;

public class ProxyClientTool {

  public static void uploadFile(CASProxyServiceStub nonBlockingStub, String fileName)
      throws Exception {
    StreamObserver<AuthProtos.FileUploadRequest> req =
        nonBlockingStub.uploadFile(
            new StreamObserver<AuthProtos.FileUploadResponse>() {
              @Override
              public void onNext(AuthProtos.FileUploadResponse value) {
                System.err.println(value.toString());
              }

              @Override
              public void onError(java.lang.Throwable t) {
                t.printStackTrace();
              }

              @Override
              public void onCompleted() {
                System.err.println("completed");
              }
            });

    File upl = Paths.get(fileName).toFile();
    FileInputStream stream = new FileInputStream(upl);

    byte[] chunk = new byte[1024];

    int bytesRead;

    while ((bytesRead = stream.read(chunk)) != -1) {
      req.onNext(
          AuthProtos.FileUploadRequest.newBuilder()
              .setIdToken("token")
              .setContent(ByteString.copyFrom(chunk, 0, bytesRead))
              .build());
    }

    req.onCompleted();
  }

  public static void downloadFile(CASProxyServiceBlockingStub stub, String sha256) {
    System.err.printf("Calling downloadFile (%s)", sha256);
    Iterator<AuthProtos.FileDownloadResponse> responseIterator =
        stub.downloadFile(
            AuthProtos.FileDownloadRequest.newBuilder()
                .setIdToken("token")
                .setSha256(sha256)
                .build());

    while (responseIterator.hasNext()) {
      AuthProtos.FileDownloadResponse resp = responseIterator.next();
      System.err.println(resp);
      System.err.println(resp.getContent().toStringUtf8());
    }
  }

  public static void main(String[] args) throws Exception {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 6000).usePlaintext().build();
    CASProxyServiceBlockingStub stub = CASProxyServiceGrpc.newBlockingStub(channel);
    CASProxyServiceStub nonBlockingStub = CASProxyServiceGrpc.newStub(channel);

    if (args.length != 2) {
      System.err.println(
          "Error: should pass exactly two arguments : <download|upload> <sha256|filename>");
      System.exit(1);
    }

    if (!args[0].equals("upload") && !args[0].equals("download")) {
      System.err.println("Error: should pass <download|upload> as first argument");
      System.exit(1);
    }

    if (args[0].equals("upload")) {
      uploadFile(nonBlockingStub.withExecutor(Executors.newSingleThreadExecutor()), args[1]);
      Thread.sleep(Long.MAX_VALUE); // sleep forever to let non-blocking stub get a response
    } else {
      downloadFile(stub, args[1]);
    }
  }
}

