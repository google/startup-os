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

import com.google.protobuf.Message;

// TODO: Convert to AutoValue
public class ProtoChange<T extends Message> {
  private T proto;
  private int newIndex;
  private int oldIndex;
  private Type type;

  public enum Type {
    ADDED,
    MODIFIED,
    REMOVED
  }

  public ProtoChange(T proto, int newIndex, int oldIndex, Type type) {
    this.proto = proto;
    this.newIndex = newIndex;
    this.oldIndex = oldIndex;
    this.type = type;
  }

  public T getProto() {
    return proto;
  }

  public int getNewIndex() {
    return newIndex;
  }

  public int getOldIndex() {
    return oldIndex;
  }

  public Type getType() {
    return type;
  }
}

