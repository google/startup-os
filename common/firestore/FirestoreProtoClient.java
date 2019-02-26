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

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.startupos.tools.reviewer.local_server.service.AuthServiceCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/** A proto wrapper for Firestore's client, that uses protos' binary format. */
public class FirestoreProtoClient {
  private static final String PROTO_FIELD = "proto";

  Firestore client;
  Storage storage;

  public FirestoreProtoClient(String serviceAccountJson) {
    try {
      InputStream serviceAccount = new FileInputStream(serviceAccountJson);
      GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
      FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).build();
      try {
        FirebaseApp.initializeApp(options);
      } catch (IllegalStateException e) {
        if (e.getMessage().contains("already exists")) {
          // Firestore is probably already initialized - do nothing
        } else {
          throw e;
        }
      }
      client = FirestoreClient.getFirestore();
      storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public FirestoreProtoClient(String project, String token) {
    GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(token, null));
    FirebaseOptions options =
        new FirebaseOptions.Builder().setCredentials(credentials).setProjectId(project).build();
    try {
      FirebaseApp.initializeApp(options);
    } catch (IllegalStateException e) {
      if (e.getMessage().contains("already exists")) {
        // Firestore is probably already initialized - do nothing
      } else {
        throw e;
      }
    }
    client = FirestoreClient.getFirestore();
  }

  public FirestoreProtoClient(AuthServiceCredentials authServiceCredentials) {
    GoogleCredentials credentials = GoogleCredentials.create(
            new AccessToken(authServiceCredentials.token(), null));
    FirebaseOptions options =
            new FirebaseOptions.Builder().setCredentials(credentials).setProjectId(
                    authServiceCredentials.projectId()).build();
    try {
      FirebaseApp.initializeApp(options);
    } catch (IllegalStateException e) {
      if (e.getMessage().contains("already exists")) {
        // Firestore is probably already initialized - do nothing
      } else {
        throw e;
      }
    }
    client = FirestoreClient.getFirestore();
    storage = StorageOptions.newBuilder().setCredentials(authServiceCredentials).build().getService();
  }

  public Firestore getClient() {
    return client;
  }

  private String joinPath(String collection, String documentId) {
    if (collection.endsWith("/")) {
      return collection + documentId;
    }
    return collection + "/" + documentId;
  }

  public static Message parseProto(DocumentSnapshot document, Message.Builder builder)
      throws InvalidProtocolBufferException {
    return builder
        .build()
        .getParserForType()
        .parseFrom(Base64.getDecoder().decode(document.getString(PROTO_FIELD)));
  }

  private ImmutableMap<String, String> encodeProto(Message proto)
      throws InvalidProtocolBufferException {
    byte[] protoBytes = proto.toByteArray();
    String base64BinaryString = Base64.getEncoder().encodeToString(protoBytes);
    return ImmutableMap.of(PROTO_FIELD, base64BinaryString);
  }

  private CollectionReference getCollectionReference(String[] parts, int length) {
    DocumentReference docRef;
    CollectionReference collectionRef = client.collection(parts[0]);
    for (int i = 1; i < length; i += 2) {
      docRef = collectionRef.document(parts[i]);
      collectionRef = docRef.collection(parts[i + 1]);
    }
    return collectionRef;
  }

  public CollectionReference getCollectionReference(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    String[] parts = path.split("/");
    if (parts.length % 2 != 1) {
      throw new IllegalArgumentException("Path length should be odd but is " + parts.length);
    }
    return getCollectionReference(parts, parts.length);
  }

  public DocumentReference getDocumentReference(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    String[] parts = path.split("/");
    if (parts.length % 2 != 0) {
      throw new IllegalArgumentException("Path length should be even but is " + parts.length);
    }
    return getCollectionReference(parts, parts.length - 1).document(parts[parts.length - 1]);
  }

  public DocumentReference getDocumentReference(String collection, String documentId) {
    return getDocumentReference(joinPath(collection, documentId));
  }

  public ApiFuture<DocumentSnapshot> getDocumentAsync(String path) {
    return getDocumentReference(path).get();
  }

  public ApiFuture<DocumentSnapshot> getDocumentAsync(String collection, String documentId) {
    return getDocumentAsync(joinPath(collection, documentId));
  }

  public DocumentSnapshot getDocument(String path) {
    try {
      return getDocumentAsync(path).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  public DocumentSnapshot getDocument(String collection, String documentId) {
    return getDocument(joinPath(collection, documentId));
  }

  public Message getProtoDocument(String path, Message.Builder builder) {
    try {
      return parseProto(getDocument(path), builder);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public Message getProtoDocument(String collection, String documentId, Message.Builder builder) {
    return getProtoDocument(joinPath(collection, documentId), builder);
  }

  public ApiFuture<WriteResult> setDocumentAsync(String path, Map map) {
    return getDocumentReference(path).set(map);
  }

  public ApiFuture<WriteResult> setDocumentAsync(
      String collection, String documentId, Map<String, ?> map) {
    return setDocumentAsync(joinPath(collection, documentId), map);
  }

  public WriteResult setDocument(String path, Map map) {
    try {
      return setDocumentAsync(path, map).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public WriteResult setDocument(String collection, String documentId, Map map) {
    return setDocument(joinPath(collection, documentId), map);
  }

  public ApiFuture<WriteResult> setProtoDocumentAsync(String path, Message proto) {
    try {
      return setDocumentAsync(path, encodeProto(proto));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException(e);
    }
  }

  public ApiFuture<WriteResult> setProtoDocumentAsync(
      String collection, String documentId, Message proto) {
    return setProtoDocumentAsync(joinPath(collection, documentId), proto);
  }

  public WriteResult setProtoDocument(String path, Message proto) {
    try {
      return setProtoDocumentAsync(path, proto).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public WriteResult setProtoDocument(String collection, String documentId, Message proto) {
    return setProtoDocument(joinPath(collection, documentId), proto);
  }

  public ApiFuture<DocumentReference> addProtoDocumentToCollectionAsync(
      String path, Message proto) {
    try {
      return getCollectionReference(path).add(encodeProto(proto));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException(e);
    }
  }

  public DocumentReference addProtoDocumentToCollection(String path, Message proto) {
    try {
      return addProtoDocumentToCollectionAsync(path, proto).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public ApiFuture<QuerySnapshot> getDocumentsAsync(String path) {
    return getCollectionReference(path).get();
  }

  public List<Message> getProtoDocuments(String path, Message.Builder builder) {
    ImmutableList.Builder<Message> result = ImmutableList.builder();
    try {
      Message proto = builder.build();
      QuerySnapshot querySnapshot = getDocumentsAsync(path).get();
      for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
        result.add(parseProto(document, builder));
      }
      return result.build();
    } catch (ExecutionException | InterruptedException | InvalidProtocolBufferException e) {
      throw new IllegalStateException(e);
    }
  }

  public MessageWithId getDocumentFromCollection(
      String path, Message.Builder builder, boolean shouldRemove) {
    try {
      QuerySnapshot querySnapshot = getCollectionReference(path).limit(1).get().get();
      if (querySnapshot.isEmpty()) {
        return null;
      }
      QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);
      MessageWithId result =
          MessageWithId.create(
              queryDocumentSnapshot.getId(), parseProto(queryDocumentSnapshot, builder));
      if (shouldRemove) {
        deleteDocument(path + "/" + queryDocumentSnapshot.getId());
      }
      return result;
    } catch (ExecutionException | InterruptedException | InvalidProtocolBufferException e) {
      throw new IllegalStateException(e);
    }
  }

  public MessageWithId getDocumentFromCollection(String path, Message.Builder proto) {
    return getDocumentFromCollection(path, proto, false);
  }

  public MessageWithId popDocument(String path, Message.Builder proto) {
    return getDocumentFromCollection(path, proto, true);
  }

  public ApiFuture<WriteResult> deleteDocumentAsync(String path) {
    return getDocumentReference(path).delete();
  }

  public ApiFuture<WriteResult> deleteDocumentAsync(String collection, String documentId) {
    return deleteDocumentAsync(joinPath(collection, documentId));
  }

  public WriteResult deleteDocument(String path) {
    try {
      return deleteDocumentAsync(path).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public WriteResult deleteDocument(String collection, String documentId) {
    return deleteDocument(joinPath(collection, documentId));
  }

  public void addCollectionListener(
      String path, Message.Builder builder, ProtoEventListener listener) {
    getCollectionReference(path)
        .addSnapshotListener(
            new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(
                  @Nullable QuerySnapshot querySnapshot, @Nullable FirestoreException e) {
                if (e != null) {
                  listener.onEvent(null, e);
                  return;
                }
                try {
                  listener.onEvent(new ProtoQuerySnapshot(querySnapshot, builder), null);
                } catch (InvalidProtocolBufferException e2) {
                  listener.onEvent(null, new IllegalArgumentException(e2));
                }
              }
            });
  }

  public String uploadTo(String bucketName, String filePath, String fileName) throws IOException {

    BlobInfo blobInfo =
        storage.create(
            BlobInfo.newBuilder(bucketName, fileName)
                //.setAcl(ImmutableList.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                .build(),
            Files.toByteArray(Paths.get(filePath).toFile()));
    return blobInfo.getMediaLink();
  }

  public String downloadFrom(String bucketName, String fileName) throws IOException {
    String[] parts = fileName.split("[.]");
    String name = parts[0];
    String extension = ".tmp";
    if (parts.length > 1) {
      extension = "." + parts[parts.length - 1];
    }
    File tempFile = File.createTempFile(name, extension);
    storage.get(BlobId.of(bucketName, fileName)).downloadTo(Paths.get(tempFile.getAbsolutePath()));
    return tempFile.getAbsolutePath();
  }
}

