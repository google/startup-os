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

package com.google.startupos.common.firestore;

import static java.net.HttpURLConnection.HTTP_OK;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auto.factory.AutoFactory;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.startupos.common.firestore.Protos.ProtoDocument;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

// TODO: Fix open Firestore rules
@AutoFactory(allowSubclasses = true)
public class FirestoreAdminClient {
  // Base path formatted by project name and path, that starts with a /.
  private final String project;
  private final String token;

  public FirestoreAdminClient(String project, String token) {
    this.project = project;
    this.token = token;
    // TODO: Finish implementation of FirestoreAdminClient
    try {
      FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountKey.json");

      FirebaseOptions options =
          new FirebaseOptions.Builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
              .build();

      FirebaseApp.initializeApp(options);
    } catch (Exception e) {
    }
  }
}

