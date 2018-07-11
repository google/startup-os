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

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.google.startupos.common.firestore.Protos.ProtoDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

// TODO: Fix open Firestore rules
public class FirestoreClient {
  // Base path formatted by project name and path, that starts with a /.
  private static final String BASE_PATH =
      "https://firestore.googleapis.com/v1beta1" + "/projects/%s/databases/(default)/documents%s";

  private final String project;
  private final String token;

  public FirestoreClient(String project, String token) {
    this.project = project;
    this.token = token;
  }

  private String getGetUrl(String path) {
    return String.format(BASE_PATH, project, path);
  }

  private String getCreateDocumentUrl(String user, String documentId) {
    // GET and CreateDocument are the same if the server selects the ID.
    if (documentId == null) {
      return getGetUrl(user);
    }
    return getGetUrl(user) + "/" + documentId;
  }

  private String getCreateDocumentUrl(String user) {
    return getCreateDocumentUrl(user, null);
  }

  public Message getDocument(String path, Message.Builder proto) {
    try {
      StringBuilder result = new StringBuilder();
      URL url = new URL(getGetUrl(path));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Bearer " + token);
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          result.append(line);
        }
      }
      if (connection.getResponseCode() != HTTP_OK) {
        throw new IllegalStateException("getDocument failed: " + connection.getResponseMessage());
      }
      FirestoreJsonFormat.parser().merge(result.toString(), proto);
      return proto.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void createProtoDocument(String path, Message proto) {
    createProtoDocument(path, null, proto);
  }

  public void createProtoDocument(String path, String documentId, Message proto) {
    byte[] protoBytes = proto.toByteArray();
    String base64BinaryString = Base64.getEncoder().encodeToString(protoBytes);
    createDocument(
        path, documentId, ProtoDocument.newBuilder().setProto(base64BinaryString).build());
  }

  public void createDocument(String path, MessageOrBuilder proto) {
    createDocument(path, null, proto);
  }

  public void createDocument(String path, String documentId, MessageOrBuilder proto) {
    try {
      createDocument(path, documentId, FirestoreJsonFormat.printer().print(proto));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createDocument(String path, String documentId, String json) {
    try {
      URL url = new URL(getCreateDocumentUrl(path, documentId));

      byte[] postDataBytes = json.getBytes("UTF-8");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // PATCH method updates the document if if exists and creates otherwise
      // java.net.HttpURLConnection does not support PATCH natively
      // SOURCE: https://stackoverflow.com/a/32503192/4617642
      connection.setRequestMethod("POST");
      if (documentId != null) {
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
      }
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
      connection.setRequestProperty("Authorization", "Bearer " + token);
      connection.setDoOutput(true);
      connection.getOutputStream().write(postDataBytes);

      InputStream stream;

      if (connection.getResponseCode() != HTTP_OK) {
        stream = connection.getErrorStream();
      } else {
        stream = connection.getInputStream();
      }

      try (Reader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
        for (int c; (c = in.read()) >= 0; ) {
          System.out.print((char) c);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

