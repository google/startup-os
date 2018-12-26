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

package com.google.startupos.tools.reviewer.job.tasks;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public abstract class FirestoreTaskBase {
  protected FirestoreProtoClient firestoreClient;

  @FlagDesc(name = "service_account_json", description = "", required = true)
  public static Flag<String> serviceAccountJson = Flag.create("");

  protected void initializeFirestoreClientIfNull() {
    if (this.firestoreClient == null) {
      FileInputStream serviceAccount = null;
      try {
        serviceAccount = new FileInputStream(serviceAccountJson.get());
        ServiceAccountCredentials cred =
            (ServiceAccountCredentials)
                GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(
                        Arrays.asList(
                            "https://www.googleapis.com/auth/cloud-platform",
                            "https://www.googleapis.com/auth/datastore"));
        this.firestoreClient =
            new FirestoreProtoClient(
                cred.getProjectId(), cred.refreshAccessToken().getTokenValue());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

