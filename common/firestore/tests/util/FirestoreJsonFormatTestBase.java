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

package com.google.startupos.common.firestore.test.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.startupos.common.firestore.FirestoreJsonFormat;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.io.IOException;
import org.junit.Before;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Test base class for {@code FirestoreJsonFormat.Parser} and {@code FirestoreJsonFormat.Printer}
 */
// TODO: Move this class to com.google.startupos.common.firestore.test.
public class FirestoreJsonFormatTestBase {
  private FileUtils fileUtils;
  private JsonParser jsonParser;
  private FirestoreJsonFormat.Printer printer;
  private FirestoreJsonFormat.Parser parser;

  @Before
  public void setup() {
    // Using real FileSystem since this test only loads files from Resources.
    TestComponent component = DaggerFirestoreJsonFormatTestBase_TestComponent.create();
    fileUtils = component.getFileUtils();
    jsonParser = new JsonParser();
    printer = FirestoreJsonFormat.printer();
    parser = FirestoreJsonFormat.parser();
  }

  protected String getGoldenFileAsString(String filename) {
    return fileUtils.readFileFromResourcesUnchecked("common/firestore/tests/resources/" + filename);
  }

  protected JsonElement getGoldenFile(String filename) {
    return jsonParser.parse(getGoldenFileAsString(filename));
  }

  protected JsonElement protoToFirestoreJson(Message proto) throws IOException {
    return jsonParser.parse(printer.print(proto));
  }

  protected Message firestoreJsonToProto(String firestoreJson, Message.Builder builder)
      throws InvalidProtocolBufferException {
    parser.merge(firestoreJson, builder);
    return builder.build();
  }

  private void printStringToLog(JsonElement json) {
    throw new RuntimeException("\n" + json.toString() + "\n");
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface TestComponent {
    FileUtils getFileUtils();
  }
}

