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

package com.google.startupos.tools.buildfilegenerator.tests;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.buildfilegenerator.ProtoFileAnalyzer;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import com.google.startupos.tools.buildfilegenerator.Protos.ProtoFile;

import static org.junit.Assert.assertEquals;

public class ProtoFileAnalyzerTest {
  private FileUtils fileUtils;
  private String testFolder;
  private ProtoFileAnalyzer protoFileAnalyzer;

  @Before
  public void setup() throws IOException {
    fileUtils = DaggerProtoFileAnalyzerTest_TestComponent.create().getFileUtils();
    testFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    protoFileAnalyzer = new ProtoFileAnalyzer(fileUtils);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface TestComponent {
    FileUtils getFileUtils();
  }

  @Test
  public void getProtoFileTest() throws IOException {
    String fileContent =
        "syntax = \"proto3\";"
            + System.lineSeparator()
            + "package com.test.package;"
            + System.lineSeparator()
            + "option java_package = \"com.test.javapackage\";"
            + System.lineSeparator()
            + "option java_outer_classname = \"Protos\";"
            + System.lineSeparator()
            + "import \"path/to/another_proto_file.proto\";"
            + System.lineSeparator()
            + "service FileService {"
            + System.lineSeparator()
            + "  rpc getFile(FileRequest) returns (FileResponse);"
            + System.lineSeparator()
            + "}"
            + System.lineSeparator()
            + "message FileRequest { string filename = 1; }"
            + System.lineSeparator()
            + "message FileResponse {"
            + System.lineSeparator()
            + "  string content = 1;"
            + System.lineSeparator()
            + "  int64 size = 2;"
            + System.lineSeparator()
            + "}"
            + System.lineSeparator()
            + "enum BooleanEnum {"
            + System.lineSeparator()
            + "    FALSE = 0;"
            + System.lineSeparator()
            + "    TRUE = 1;"
            + System.lineSeparator()
            + "}"
            + System.lineSeparator();

    String filePath = fileUtils.joinToAbsolutePath(testFolder, "test_proto.proto");
    fileUtils.writeStringUnchecked(fileContent, filePath);

    ProtoFile expectedProtoFile =
        ProtoFile.newBuilder()
            .setPackage("com.test.package")
            .setFileName("test_proto")
            .setJavaPackage("com.test.javapackage")
            .setJavaOuterClassname("Protos")
            .addAllMessages(Arrays.asList("FileRequest", "FileResponse"))
            .addAllServices(Arrays.asList("FileService"))
            .addAllEnums(Arrays.asList("BooleanEnum"))
            .build();

    assertEquals(expectedProtoFile, protoFileAnalyzer.getProtoFile(filePath));
  }
}

