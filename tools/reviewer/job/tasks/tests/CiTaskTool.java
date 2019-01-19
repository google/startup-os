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
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.reviewer.ReviewerConstants;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest.Target;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.tools.reviewer.local_server.service.AuthService;
import dagger.Component;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/** A tool for testing CiTask. */
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
    int id = 100;
    client.setProtoDocument(
        ReviewerConstants.CI_REQUESTS_PATH + "/" + id,
        CiRequest.newBuilder()
            .setDiffId(id)
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

