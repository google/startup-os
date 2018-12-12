package com.google.startupos.common.firestore;

import com.google.auto.value.AutoValue;
import com.google.protobuf.Message;

@AutoValue
public abstract class MessageWithId {
    public abstract String id();
    public abstract Message message();

    static MessageWithId create(String id, Message msg) {
        return new AutoValue_MessageWithId(id, msg);
    }
}
