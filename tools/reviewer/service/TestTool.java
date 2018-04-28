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

package com.google.startupos.tools.reviewer.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import com.google.startupos.tools.reviewer.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.Diff;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

/** Test tool for CodeReviewService. */
public class TestTool {
  private final ManagedChannel channel;
  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;

  private TestTool() {
    channel = ManagedChannelBuilder.forAddress("localhost", 8001).usePlaintext(true).build();
    blockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  private void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  private String getFile(String name) {
    final FileRequest request = FileRequest.newBuilder().setFilename(name).build();
    try {
      return blockingStub.getFile(request).getContent();
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void createDiff(Diff diff) {
    final CreateDiffRequest request = CreateDiffRequest.newBuilder().setDiff(diff).build();
    try {
      blockingStub.createDiff(request);
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    Diff diff = Diff.newBuilder().setNumber(123).build();
    createDiff(diff);
  }

  public static void main(String[] args) {
    TestTool tool = new TestTool();
    tool.run();
  }
}
