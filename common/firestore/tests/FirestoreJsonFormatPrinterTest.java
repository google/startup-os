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

import com.google.startupos.common.firestore.test.util.FirestoreJsonFormatTestBase;
import com.google.gson.JsonElement;
import com.google.protobuf.ByteString;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * Tests for FirestoreJsonFormat.Printer class. Formatted jsons are compared to golden files in
 * /resources. Printer takes protobuf Message and formats it as Firestore JSON
 */
public class FirestoreJsonFormatPrinterTest extends FirestoreJsonFormatTestBase {

  /** Tests printing a proto with several primitives. */
  @Test
  public void primitivesTest() throws Exception {
    JsonElement jsonElement =
        protoToFirestoreJson(
            Item.newBuilder()
                .setDoubleField1(12.34)
                .setFloatField1(56.78f)
                .setIntField1(123)
                .setLongField1(456L)
                .setBooleanField1(true)
                .setStringField1("Some string")
                .setBytesField1(ByteString.copyFrom("abcd", UTF_8))
                .build());
    assertEquals(getGoldenFile("primitives_test.json"), jsonElement);
  }

  /** Tests printing a proto with a message field. */
  @Test
  public void protoMessageTest() throws Exception {
    JsonElement jsonElement =
        protoToFirestoreJson(
            ItemList.newBuilder()
                .setSingleItem(Item.newBuilder().setDoubleField1(12.34).build())
                .build());
    assertEquals(getGoldenFile("proto_message_test.json"), jsonElement);
  }

  /** Tests printing a proto with a repeated primitives field. */
  @Test
  public void repeatedPrimitivesTest() throws Exception {
    JsonElement jsonElement =
        protoToFirestoreJson(ItemList.newBuilder().addIntValue(123).addIntValue(456).build());

    assertEquals(getGoldenFile("repeated_primitive_test.json"), jsonElement);
  }

  /** Tests printing a proto with a repeated messages field. */
  @Test
  public void repeatedProtoMessageTest() throws Exception {
    JsonElement jsonElement =
        protoToFirestoreJson(
            ItemList.newBuilder()
                .addItem(Item.newBuilder().setDoubleField1(12.34).build())
                .addItem(Item.newBuilder().setDoubleField1(56.78).build())
                .build());
    assertEquals(getGoldenFile("repeated_proto_message_test.json"), jsonElement);
  }
}

