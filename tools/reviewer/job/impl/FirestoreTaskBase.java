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

package com.google.startupos.tools.reviewer.job.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.startupos.common.firestore.FirestoreClient;
import com.google.startupos.common.firestore.FirestoreClientFactory;
import com.google.startupos.tools.reviewer.job.ReviewerJob;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public abstract class FirestoreTaskBase {
  protected FirestoreClient firestoreClient;
  protected FirestoreClientFactory firestoreClientFactory;

  protected void initializeFirestoreClientIfNull() {
    if (this.firestoreClientFactory == null) {
      throw new IllegalArgumentException("this.firestoreClientFactory should be initialized");
    }

    if (this.firestoreClient == null) {
      FileInputStream serviceAccount = null;
      try {
        serviceAccount = new FileInputStream(ReviewerJob.serviceAccountJson.get());
        ServiceAccountCredentials cred =
            (ServiceAccountCredentials)
                GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(
                        Arrays.asList(
                            "https://www.googleapis.com/auth/cloud-platform",
                            "https://www.googleapis.com/auth/datastore"));
        this.firestoreClient =
            this.firestoreClientFactory.create(
                cred.getProjectId(), cred.refreshAccessToken().getTokenValue());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

