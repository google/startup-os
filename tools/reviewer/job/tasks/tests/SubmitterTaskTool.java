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

package com.google.startupos.tools.reviewer.job.tasks.tests;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.ReviewerConstants;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest.Target;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse.TargetResult;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import dagger.Component;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/** A tool for testing SubmitterTask. */
@Singleton
public class SubmitterTaskTool {
  private FileUtils fileUtils;
  private AuthService authService;
  private FirestoreProtoClient client;

  @Inject
  SubmitterTaskTool(FileUtils fileUtils, AuthService authService) {
    this.fileUtils = fileUtils;
    this.authService = authService;
  }

  void run() {
    client = new FirestoreProtoClient(authService.getProjectId(), authService.getToken());
    int id = 1234;
    client.setProtoDocument(
        ReviewerConstants.DIFF_COLLECTION + "/" + id,
        Diff.newBuilder()
            .setId(id)
            .setStatus(Diff.Status.SUBMITTING)
            .addCiResponse(
                CiResponse.newBuilder()
                    .addResult(
                        TargetResult.newBuilder().setStatus(TargetResult.Status.SUCCESS).build())
                    .build())
            .build());
    System.exit(0);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    SubmitterTaskTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parse(args, AuthService.class.getPackage());
    DaggerSubmitterTaskTool_ToolComponent.create().getTool().run();
  }
}

