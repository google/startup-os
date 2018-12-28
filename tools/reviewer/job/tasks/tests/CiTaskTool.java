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

import com.google.startupos.common.CommonModule;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import java.io.IOException;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import java.util.concurrent.Executors;
import com.google.startupos.tools.localserver.service.AuthService;
import java.util.Date;
import com.google.cloud.firestore.DocumentReference;
import com.google.api.core.ApiFuture;
import java.util.Map;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;
import java.util.HashMap;
import com.google.cloud.firestore.EventListener;
import java.util.concurrent.ExecutionException;
import com.google.cloud.firestore.DocumentSnapshot;
import javax.annotation.Nullable;
import com.google.cloud.firestore.FirestoreException;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest.Target;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;

/** A tool for testing TextDifferencer. */
@Singleton
public class CiTaskTool {
  private FileUtils fileUtils;
  private AuthService authService;
  private FirestoreProtoClient client;

  @Inject
  CiTaskTool(FileUtils fileUtils, AuthService authService) {
    this.fileUtils = fileUtils;
    this.authService = authService;
  }

  void run() {
    client = new FirestoreProtoClient(authService.getProjectId(), authService.getToken());
    client.setProtoDocument(
        "/reviewer/ci/requests/100",
        CiRequest.newBuilder()
            .setDiffId(100)
            .addTarget(
                Target.newBuilder()
                    .setRepo(
                        Repo.newBuilder()
                            .setId("startup-os")
                            .setUrl("https://github.com/google/startup-os")
                            .build())
                    .setCommitId("5b599dd74f58ff5716520db5f724fdf7a75ca419")
                    .build())
            .build());
    System.exit(0);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    CiTaskTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parse(args, AuthService.class.getPackage());
    DaggerCiTaskTool_ToolComponent.create().getTool().run();
  }
}

