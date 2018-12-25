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

package com.google.startupos.common.firestore.tests;

import com.google.startupos.common.CommonModule;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
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

/** A tool for testing TextDifferencer. */
@Singleton
public class FirestoreClientTool {
  @FlagDesc(name = "service_account_json", description = "")
  public static Flag<String> serviceAccountJson = Flag.create("");

  private FileUtils fileUtils;
  private AuthService authService;
  private FirestoreProtoClient client;

  @Inject
  FirestoreClientTool(FileUtils fileUtils, AuthService authService) {
    this.fileUtils = fileUtils;
    this.authService = authService;
  }

  void run() throws IOException {
    authService.refreshToken();
    client = new FirestoreProtoClient(authService.getProjectId(), authService.getToken());

    Executors.newSingleThreadExecutor()
        .execute(
            new Runnable() {
              @Override
              public void run() {
                testFunctionality();
              }
            });
    try {
      Thread.sleep(100000000);
    } catch (Exception e) {
    }
  }

  public void testFunctionality() {
    WriteResult result = client.setProtoDocument("test/bla", Diff.newBuilder().setId(123).build());
    System.out.println("Update time : " + result.getUpdateTime());

    System.out.println(client.getProtoDocument("reviewer/data/diff/100", Diff.newBuilder()));

    System.out.println(
        client.getDocumentFromCollection("reviewer/data/diff", Diff.newBuilder(), false));

    DocumentReference docRef = client.getClient().collection("users").document("alovelace");
    docRef.addSnapshotListener(
        new EventListener<DocumentSnapshot>() {
          @Override
          public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirestoreException e) {
            if (e != null) {
              System.err.println("Listen failed: " + e);
              return;
            }

            if (snapshot != null && snapshot.exists()) {
              System.out.println("Current data: " + snapshot.getData());
            } else {
              System.out.print("Current data: null");
            }
          }
        });
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    FirestoreClientTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parse(args, FirestoreClientTool.class.getPackage(), AuthService.class.getPackage());
    DaggerFirestoreClientTool_ToolComponent.create().getTool().run();
  }
}

