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

package com.google.startupos.tools.reviewer.localserver.service.tests;

import com.google.startupos.tools.reviewer.localserver.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffFilesRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffFilesResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import com.google.startupos.tools.reviewer.localserver.service.Protos.CreateDiffRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.tools.reviewer.localserver.service.Protos.FileRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.TextDiffRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.TextDiffResponse;

/* Test tool for CodeReviewService.
 *
 * Alternatively, to run grpc_polyglot (curl-like tool for gRPC), use:
 * cat tools/reviewer/local_server/service/tests/get_diff_files_request.json | bazel run //tools:grpc_polyglot -- --command=call --endpoint=localhost:8001 --full_method=com.google.startupos.tools.reviewer.localserver.service.CodeReviewService/getDiffFiles
 * cat tools/reviewer/local_server/service/tests/get_text_diff_request.json | bazel run //tools:grpc_polyglot -- --command=call --endpoint=localhost:8001 --full_method=com.google.startupos.tools.reviewer.localserver.service.CodeReviewService/getTextDiff
 * cat tools/reviewer/local_server/service/tests/get_diff_request.json | bazel run //tools:grpc_polyglot -- --command=call --endpoint=localhost:8001 --full_method=com.google.startupos.tools.reviewer.localserver.service.CodeReviewService/getDiff
 */

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

  private TextDiffResponse getTextDiff(File leftFile, File rightFile) {
    final TextDiffRequest request =
        TextDiffRequest.newBuilder().setLeftFile(leftFile).setRightFile(rightFile).build();
    try {
      return blockingStub.getTextDiff(request);
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void runGetTextDiff() {
    File leftFile =
        File.newBuilder()
            .setRepoId("startup-os")
            .setCommitId("112da27b321ed6aa2ec1bc91f3918eb41d8a938c")
            .setFilename("WORKSPACE")
            .build();
    File rightFile =
        File.newBuilder()
            .setRepoId("startup-os")
            .setCommitId("112da27b321ed6aa2ec1bc91f3918eb41d8a938c")
            .setFilename("WORKSPACE")
            .build();
    System.out.println(getTextDiff(leftFile, rightFile));
  }

  public void runGetFile() {
    System.out.println(getFile("WORKSPACE"));
  }

  public DiffFilesResponse getDiffFiles(String workspace, long diffNumber) {
    DiffFilesRequest request =
        DiffFilesRequest.newBuilder().setWorkspace(workspace).setDiffId(diffNumber).build();
    try {
      return blockingStub.getDiffFiles(request);
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void runGetDiffFiles(String workspace, long diffNumber) {
    System.out.println(getDiffFiles(workspace, diffNumber));
  }

  public Diff getDiff(long diffNumber) {
    DiffRequest request = DiffRequest.newBuilder().setDiffId(diffNumber).build();

    return blockingStub.getDiff(request);
  }

  public void runGetDiff(long diffNumber) {
    System.out.println(getDiff(diffNumber));
  }

  public static void main(String[] args) {
    TestTool tool = new TestTool();
    tool.runGetTextDiff();
  }
}

