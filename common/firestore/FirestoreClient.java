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

import com.google.auto.factory.AutoFactory;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.startupos.common.firestore.Protos.ProtoDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;

// TODO: Fix open Firestore rules
@AutoFactory(allowSubclasses = true)
public class FirestoreClient {
  // Base path formatted by project name and path, that starts with a /.
  private static final String API_ROOT = "https://firestore.googleapis.com/v1beta1/";
  private static final String BASE_PATH = API_ROOT + "projects/%s/databases/(default)/documents%s";

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

  private String httpMethod(String urlString, String method) throws IOException {
    StringBuilder response = new StringBuilder();
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Authorization", "Bearer " + token);
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    }
    if (connection.getResponseCode() != HTTP_OK) {
      throw new IllegalStateException("httpGet failed: " + connection.getResponseMessage());
    }
    return response.toString();
  }

  private String httpGet(String urlString) throws IOException {
    return this.httpMethod(urlString, "GET");
  }

  private void httpDelete(String urlString) throws IOException {
    this.httpMethod(urlString, "DELETE");
  }

  private String getDocumentResponse(String path) throws IOException {
    return this.httpGet(getGetUrl(path));
  }

  public Message getDocument(String path, Message.Builder proto) {
    try {
      FirestoreJsonFormat.parser().merge(getDocumentResponse(path), proto);
      return proto.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Message getProtoDocument(String path, Message.Builder proto) {
    try {
      ProtoDocument.Builder protoDocument = ProtoDocument.newBuilder();
      FirestoreJsonFormat.parser().merge(getDocumentResponse(path), protoDocument);
      return fromProtoDocument(protoDocument.build(), proto);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Message fromProtoDocument(ProtoDocument protoDocument, Message.Builder proto)
      throws InvalidProtocolBufferException {
    byte[] protoBytes = Base64.getDecoder().decode(protoDocument.getProto());
    // We just need the proto Message to get a parser
    return proto.build().getParserForType().parseFrom(protoBytes);
  }

  public void createProtoDocument(String path, Message proto) {
    createProtoDocument(path, null, proto);
  }

  // TODO: merge createProtoDocument and createDocument to
  // createDocument(String path, String documentId, bool binaryFormat, bool jsonFormat)

  // TODO: check whether path starts with slash
  public void createProtoDocument(String path, String documentId, Message proto) {
    byte[] protoBytes = proto.toByteArray();
    String base64BinaryString = Base64.getEncoder().encodeToString(protoBytes);
    createDocument(
        path, documentId, ProtoDocument.newBuilder().setProto(base64BinaryString).build());
  }

  public void createDocument(String path, MessageOrBuilder proto) {
    createDocument(path, null, proto);
  }

  // TODO: check whether path starts with slash
  public void createDocument(String path, String documentId, MessageOrBuilder proto) {
    try {
      createDocument(path, documentId, FirestoreJsonFormat.printer().print(proto));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private MessageWithId getDocumentFromCollection(
      String path, Message.Builder proto, boolean shouldRemove) {
    // TODO: pop a document without fetching all documents in the list
    try {
      // this is *document* list, containing 'Firestore-flavored' JSON documents
      String documentList = this.httpGet(getGetUrl(path));
      JsonElement element = new JsonParser().parse(documentList);
      JsonArray documents = element.getAsJsonObject().getAsJsonArray("documents");
      if (documents != null && documents.size() > 0) {
        JsonObject document = documents.get(documents.size() - 1).getAsJsonObject();
        FirestoreJsonFormat.parser().merge(document.toString(), proto);
        String keyName = document.get("name").getAsJsonPrimitive().getAsString();
        // TODO: refactor to have separate .deleteDocument() method
        // keyName starts with 'projects/' so it's full path rather than only document id
        if (shouldRemove) {
          this.httpDelete(API_ROOT + keyName);
        }
        return MessageWithId.create(keyName.substring(keyName.lastIndexOf('/') + 1), proto.build());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Pops (FIFO) document from collection
   *
   * @param path document path
   * @param proto builder for proto message
   * @return proto message
   */
  public MessageWithId popDocument(String path, Message.Builder proto) {
    return getDocumentFromCollection(path, proto, true);
  }

  /**
   * Gets (FIFO) document from collection
   *
   * @param path document path
   * @param proto builder for proto message
   * @return proto message
   */
  public MessageWithId getDocumentFromCollection(String path, Message.Builder proto) {
    return getDocumentFromCollection(path, proto, false);
  }

  /**
   * Gets a (homogeneous) list of documents from collection;
   *
   * @param path document path
   * @param proto builder for proto message
   * @return list of proto messages
   */
  public List<Message> listDocuments(String path, Message.Builder proto) {
    try {
      // this is *document* list, containing 'Firestore-flavored' JSON documents
      String documentList = this.httpGet(getGetUrl(path));
      JsonElement element = new JsonParser().parse(documentList);
      JsonArray documents = element.getAsJsonObject().getAsJsonArray("documents");
      ImmutableList.Builder<Message> result = ImmutableList.builder();

      if (documents != null && documents.size() > 0) {
        for (JsonElement document : documents) {
          proto.clear();
          ProtoDocument.Builder protoDocumentBuilder = ProtoDocument.newBuilder();
          FirestoreJsonFormat.parser().merge(document.toString(), protoDocumentBuilder);
          result.add(fromProtoDocument(protoDocumentBuilder.build(), proto));
        }
      }
      return result.build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

