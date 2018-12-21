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
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseApp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.startupos.common.firestore.Protos.ProtoDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import java.util.concurrent.ExecutionException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FirestoreAdminClient {
  Firestore client;

  public FirestoreAdminClient(String serviceAccountJson) {
    try {
      InputStream serviceAccount = new FileInputStream(serviceAccountJson);
      GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
      FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).build();
      FirebaseApp.initializeApp(options);

      client = FirestoreClient.getFirestore();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // TODO: Replace method with actual usage
  public void testFunctionality() {
    try {
      DocumentReference docRef = client.collection("users").document("alovelace2");
      // Add document data  with id "alovelace" using a hashmap
      Map<String, Object> data = new HashMap<>();
      data.put("first", "Ada");
      data.put("last", "Lovelace");
      data.put("born", 1815);
      ApiFuture<WriteResult> result = docRef.set(data);
      System.out.println("Update time : " + result.get().getUpdateTime());

      CollectionReference reference = client.collection("users");
      ApiFuture<QuerySnapshot> query = reference.get();
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
      for (QueryDocumentSnapshot document : documents) {
        System.out.println("User: " + document.getId());
        System.out.println("First: " + document.getString("first"));
        System.out.println("Last: " + document.getString("last"));
        System.out.println("Born: " + document.getLong("born"));
      }
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}

