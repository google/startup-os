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

package com.google.startupos.common.firestore.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import com.google.startupos.common.firestore.test.util.FirestoreJsonFormatTestBase;
import com.google.protobuf.ByteString;
import org.junit.Test;

/**
 * Tests for FirestoreJsonFormat.Parser class. Parsed jsons are compared to printed jsons using the
 * Printer class. Parser takes Firestore JSON and formats it as protobuf Message
 */
public class FirestoreJsonFormatParserTest extends FirestoreJsonFormatTestBase {

  @Test
  public void primitivesTest() throws Exception {
    Item reference =
        Item.newBuilder()
            .setDoubleField1(12.34)
            .setFloatField1(56.78f)
            .setIntField1(123)
            .setLongField1(456)
            .setBooleanField1(true)
            .setStringField1("Some string")
            .setBytesField1(ByteString.copyFrom("abcd", UTF_8))
            .build();

    Item message =
        (Item)
            firestoreJsonToProto(getGoldenFileAsString("primitives_test.json"), Item.newBuilder());

    assertEquals(reference, message);
  }

  @Test
  public void protoMessageTest() throws Exception {
    ItemList reference =
        ItemList.newBuilder()
            .setSingleItem(Item.newBuilder().setDoubleField1(12.34).build())
            .build();

    ItemList message =
        (ItemList)
            firestoreJsonToProto(
                getGoldenFileAsString("proto_message_test.json"), ItemList.newBuilder());

    assertEquals(reference, message);
  }

  @Test
  public void repeatedPrimitivesTest() throws Exception {
    ItemList reference = ItemList.newBuilder().addIntValue(123).addIntValue(456).build();

    ItemList message =
        (ItemList)
            firestoreJsonToProto(
                getGoldenFileAsString("repeated_primitive_test.json"), ItemList.newBuilder());

    assertEquals(reference, message);
  }

  @Test
  public void repeatedProtoMessageTest() throws Exception {
    ItemList reference =
        ItemList.newBuilder()
            .addItem(Item.newBuilder().setDoubleField1(12.34).build())
            .addItem(Item.newBuilder().setDoubleField1(56.78).build())
            .build();

    ItemList message =
        (ItemList)
            firestoreJsonToProto(
                getGoldenFileAsString("repeated_proto_message_test.json"), ItemList.newBuilder());

    assertEquals(reference, message);
  }
}

