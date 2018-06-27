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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import java.util.concurrent.TimeUnit;
import com.google.startupos.tools.localserver.service.Protos.AuthDataRequest;
import com.google.startupos.tools.localserver.service.AuthServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;

public class LocalHttpGatewayGrpcClient {

  private final ManagedChannel channel;
  private final AuthServiceGrpc.AuthServiceBlockingStub authBlockingStub;
  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub codeReviewBlockingStub;

  public LocalHttpGatewayGrpcClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());
  }

  public LocalHttpGatewayGrpcClient(String inProcessServerName) {
    this(InProcessChannelBuilder.forName(inProcessServerName).directExecutor().build());
  }

  private LocalHttpGatewayGrpcClient(ManagedChannel channel) {
    this.channel = channel;
    authBlockingStub = AuthServiceGrpc.newBlockingStub(channel);
    codeReviewBlockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public String getFile(String name) {
    final FileRequest request = FileRequest.newBuilder().setFilename(name).build();
    try {
      return codeReviewBlockingStub.getFile(request).getContent();
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void postAuthData(String projectId, String apiKey, String jwtToken, String refreshToken) {
    final AuthDataRequest request =
        AuthDataRequest.newBuilder()
            .setProjectId(projectId)
            .setApiKey(apiKey)
            .setJwtToken(jwtToken)
            .setRefreshToken(refreshToken)
            .build();
    try {
      authBlockingStub.postAuthData(request);
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
  }

  public CodeReviewServiceGrpc.CodeReviewServiceBlockingStub getCodeReviewStub() {
    return codeReviewBlockingStub;
  }
}

