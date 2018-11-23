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

package com.google.startupos.tools.reviewer.job.sync;

import com.google.startupos.tools.reviewer.localserver.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ReviewerClient {
  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;

  public ReviewerClient() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 8001).usePlaintext(true).build();
    blockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  public Diff getDiff(long diffNumber) {
    DiffRequest request = DiffRequest.newBuilder().setDiffId(diffNumber).build();
    return blockingStub.getDiff(request);
  }
}

