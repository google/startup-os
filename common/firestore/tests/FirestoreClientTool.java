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

import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableList;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.firestore.ProtoChange;
import com.google.startupos.common.firestore.ProtoEventListener;
import com.google.startupos.common.firestore.ProtoQuerySnapshot;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.local_server.service.AuthService;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Diff;
import dagger.Component;
import java.io.IOException;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/** A tool for testing FirestoreProtoClient. */
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

  void run() {
    System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
    authService.refreshToken();
    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX " + authService.getTokenExpiration());
    client = new FirestoreProtoClient(serviceAccountJson.get());

    Executors.newSingleThreadExecutor().execute(() -> testFunctionality());
    try {
      Thread.sleep(100000000);
    } catch (Exception ignored) {
    }
  }

  public void testFunctionality() {
    WriteResult result = client.setProtoDocument("test/bla", Diff.newBuilder().setId(123).build());
    System.out.println("Update time : " + result.getUpdateTime());

    client.addCollectionListener("/reviewer/ci/requests", CiRequest.newBuilder(),
        new ProtoEventListener<ProtoQuerySnapshot<CiRequest>>() {
          @Override
          public void onEvent(@Nullable ProtoQuerySnapshot<CiRequest> snapshot, @Nullable RuntimeException e) {
            if (e != null) {
              System.err.println("Listen failed: " + e);
              return;
            }
            ImmutableList<CiRequest> protos = snapshot.getProtos();
            for (CiRequest ciRequest : protos) {
              System.out.println(ciRequest);
            }
            ImmutableList<ProtoChange<CiRequest>> protoChanges = snapshot.getProtoChanges();
            for (ProtoChange<CiRequest> protoChange : protoChanges) {
              System.out.println(protoChange.getProto());
              System.out.println(protoChange.getType());
              System.out.println(protoChange.getOldIndex());
              System.out.println(protoChange.getNewIndex());
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
    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    DaggerFirestoreClientTool_ToolComponent.create().getTool().run();
  }
}
