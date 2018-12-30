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

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/** A proto wrapper for Firestore's QuerySnapshot. */
public class ProtoQuerySnapshot<T extends Message> {
  private ImmutableList<T> protos;
  private ImmutableList<ProtoChange<T>> protoChanges;

  public ProtoQuerySnapshot(QuerySnapshot querySnapshot, Message.Builder builder)
      throws InvalidProtocolBufferException {
    // TODO: Avoid parsing the same objects twice in getDocuments() and getDocumentChanges().
    ImmutableList.Builder<T> protos = ImmutableList.builder();
    for (DocumentSnapshot docSnapshot : querySnapshot.getDocuments()) {
      protos.add((T) FirestoreProtoClient.parseProto(docSnapshot, builder));
    }
    this.protos = protos.build();
    ImmutableList.Builder<ProtoChange<T>> protoChanges = ImmutableList.builder();
    for (DocumentChange docChange : querySnapshot.getDocumentChanges()) {
      T proto = (T) FirestoreProtoClient.parseProto(docChange.getDocument(), builder);
      protoChanges.add(
          new ProtoChange<T>(
              proto,
              docChange.getNewIndex(),
              docChange.getOldIndex(),
              convertChangeType(docChange.getType())));
    }
    this.protoChanges = protoChanges.build();
  }

  ProtoChange.Type convertChangeType(DocumentChange.Type type) {
    switch (type) {
      case ADDED:
        return ProtoChange.Type.ADDED;
      case MODIFIED:
        return ProtoChange.Type.MODIFIED;
      case REMOVED:
        return ProtoChange.Type.REMOVED;
      default:
        throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  public ImmutableList<T> getProtos() {
    return protos;
  }

  public ImmutableList<ProtoChange<T>> getProtoChanges() {
    return protoChanges;
  }
}

