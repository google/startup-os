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

package com.google.startupos.common;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.protobuf.ByteString;
import com.google.startupos.common.tests.Protos.EnumMessage;
import com.google.startupos.common.tests.Protos.MapMessage;
import com.google.startupos.common.tests.Protos.Parent;
import com.google.startupos.common.tests.Protos.ScalarTypesMessage;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class FileUtilsTest {

  private static final String TEST_DIR_PATH = "/foo";
  private static final String TEST_FILE_PATH = "/foo.txt";
  private static final String TEST_PROTO_BINARY_FILE_PATH = "/foo.pb";
  private static final String TEST_PROTOTXT_FILE_PATH = "/test.prototxt";

  @Parameters(name = "{1}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][]{
            {Configuration.unix(), "Unix"},
            {Configuration.osX(), "OSX"},
            {Configuration.windows(), "Windows"}
    });
  }

  private Configuration fileSystemConfig;
  private String fileSystemName;
  private FileSystem fileSystem;
  private FileUtils fileUtils;

  public FileUtilsTest(Configuration fileSystemConfig, String fileSystemName) {
    this.fileSystemConfig = fileSystemConfig;
    this.fileSystemName = fileSystemName;
  }

  @Before
  public void setup() {
    fileSystem = Jimfs.newFileSystem(fileSystemConfig);
    CommonComponent commonComponent = DaggerCommonComponent.builder().commonModule(new CommonModule() {
      @Provides @Singleton @Override public FileSystem provideDefaultFileSystem() {
        return fileSystem;
      }
    }).build();
    fileUtils = commonComponent.geFileUtils();
  }

  @Test
  public void testFolderExistsIsTrueWhenFolder() throws IOException {
    // TODO: Add tests for Windows. Currently, jimfs says: "Jimfs does not currently support the
    //     "Windows syntax for an absolute path on the current drive".
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectory(testPath);
    assertTrue(fileUtils.folderExists(TEST_DIR_PATH));
  }

  @Test
  public void testFolderExistsIsFalseWhenFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("hello world"), UTF_8);
    assertFalse(fileUtils.folderExists(TEST_FILE_PATH));
  }

  @Test
  public void testFolderExistsIsFalseWhenNothing() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    assertFalse(fileUtils.folderExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileExistsIsTrueWhenFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("hello world"), UTF_8);
    assertTrue(fileUtils.fileExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileExistsIsFalseWhenFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectory(testPath);
    assertFalse(fileUtils.fileExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileExistsIsFalseWhenNothing() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    assertFalse(fileUtils.fileExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileOrFolderExistsIsTrueWhenFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("hello world"), UTF_8);
    assertTrue(fileUtils.fileOrFolderExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileOrFolderExistsIsTrueWhenFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectory(testPath);
    assertTrue(fileUtils.fileOrFolderExists(TEST_DIR_PATH));
  }

  @Test
  public void testFileOrFolderExistsIsFalseWhenNothing() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    assertFalse(fileUtils.fileOrFolderExists(TEST_DIR_PATH));
    assertFalse(fileUtils.fileOrFolderExists(TEST_FILE_PATH));
  }

  @Test
  public void testMkdirsWhenFolder() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    fileUtils.mkdirs(TEST_DIR_PATH);
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH)));
  }

  @Test
  public void testMkdirsWhenParentFolderNonExists() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    fileUtils.mkdirs(TEST_DIR_PATH + "/foo2");
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH + "/foo2")));
  }

  @Test
  public void testMkdirsWhenPathContainsPoint() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    fileUtils.mkdirs(TEST_FILE_PATH);
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_FILE_PATH)));
  }

  @Test
  public void testExpandHomeDirectoryWhenPathStartsWithTilde() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    String path = fileUtils.expandHomeDirectory("~" + TEST_DIR_PATH);
    assertEquals(System.getProperty("user.home") + TEST_DIR_PATH, path);
  }

  @Test
  public void testExpandHomeDirectoryWhenPathDoesNotStartWithTilde() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    String path = fileUtils.expandHomeDirectory(TEST_DIR_PATH);
    assertEquals(TEST_DIR_PATH, path);
  }

  @Test
  public void testExpandHomeDirectoryWhenPathIsEmptyString() {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    String path = fileUtils.expandHomeDirectory("");
    assertEquals("", path);
  }

  @Test
  public void testWriteStringWhenFileIsEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    fileUtils.writeString("hello world", TEST_FILE_PATH);
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("hello world"));
  }

  @Test
  public void testWriteStringWhenFileIsNotEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("first line"), UTF_8);
    fileUtils.writeString("second line", TEST_FILE_PATH);
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("second line"));
    assertFalse(Files.readAllLines(testPath, UTF_8).contains("first line"));
  }

  @Test
  public void testWriteStringWhenFileWithoutExtension() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    fileUtils.writeString("hello world", TEST_DIR_PATH);
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("hello world"));
  }

  @Test
  public void testWriteStringWhenParentFolderExists() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Path testPath = fileSystem.getPath(TEST_DIR_PATH + TEST_FILE_PATH);
    fileUtils.writeString("hello world", TEST_DIR_PATH + TEST_FILE_PATH);
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("hello world"));
  }

  @Test
  public void testWriteStringWhenParentFolderNonExists() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH + TEST_FILE_PATH);
    fileUtils.writeString("hello world", TEST_DIR_PATH + TEST_FILE_PATH);
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("hello world"));
  }

  @Test(expected = RuntimeException.class)
  public void testWriteStringUnchackedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.writeStringUnchecked("hello world", "");
  }

  @Test
  public void testReadFileWhenOneLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("first line"), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\n", content);
  }

  @Test
  public void testReadFileWhenTwoLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of("first line", "second line"), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\nsecond line\n", content);
  }

  @Test
  public void testReadFileWhenIsEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.write(testPath, ImmutableList.of(), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("", content);
  }

  @Test(expected = RuntimeException.class)
  public void testReadFileUnchackedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readFileUnchecked("");
  }

  @Test
  public void testGetFilesWhenOneFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + TEST_FILE_PATH));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of("foo.txt"), fileNames);
  }

  @Test
  public void testGetFilesWhenFileWithoutExtension() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/foo"));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of("foo"), fileNames);
  }

  @Test
  public void testGetFilesWhenTwoFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/first_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/second_file.txt"));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of("first_file.txt", "second_file.txt"), fileNames);
  }

  @Test
  public void testGetFilesWhenPathDoesNotContainFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of(), fileNames);
  }

  @Test
  public void testGetFilesWhenPathContainOnlyFolders() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/first_dir"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/second_dir"));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of(), fileNames);
  }

  @Test
  public void testGetFilesWhenFileInSubdirectory() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/subdirectory"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/subdirectory" + TEST_FILE_PATH));
    ImmutableList<String> fileNames = fileUtils.getFiles(TEST_DIR_PATH);
    assertEquals(ImmutableList.of(), fileNames);
  }

  @Test
  public void testReadPrototxtWithScalarTypes() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.write(testPath, ImmutableList.of(
            "double_field: 123.4",
            "float_field: 432.1",
            "int32_field: 123",
            "int64_field: 321",
            "bool_field: true",
            "string_field: \"foo\"",
            "bytes_field: \"foo\""),
            UTF_8);
    ScalarTypesMessage actual =
            (ScalarTypesMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, ScalarTypesMessage.newBuilder());
    ScalarTypesMessage expected = ScalarTypesMessage.newBuilder()
            .setDoubleField(123.4)
            .setFloatField(432.1f)
            .setInt32Field(123)
            .setInt64Field(321L)
            .setBoolField(true)
            .setStringField("foo")
            .setBytesField(ByteString.copyFrom("foo".getBytes()))
            .build();
    assertEquals(expected, actual);
  }

  @Test
  public void testReadPrototxtWithEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.write(testPath, ImmutableList.of(
            "favorite: GREEN"),
            UTF_8);
    EnumMessage actual =
            (EnumMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, EnumMessage.newBuilder());
    EnumMessage expected = EnumMessage.newBuilder()
            .setFavorite(EnumMessage.Color.GREEN)
            .build();
    assertEquals(expected, actual);
  }

  @Test
  public void testReadPrototxtWithRepeatedEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.write(testPath, ImmutableList.of(
            "additional: BLUE",
            "additional: RED"),
            UTF_8);
    EnumMessage actual =
            (EnumMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, EnumMessage.newBuilder());
    EnumMessage expected = EnumMessage.newBuilder()
            .addAllAdditional(ImmutableList.of(EnumMessage.Color.BLUE, EnumMessage.Color.RED))
            .build();
    assertEquals(expected, actual);
  }

  @Test
  public void testReadPrototxtWithNestedType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.write(testPath, ImmutableList.of(
            "child {",
            "name: \"foo\"",
            "}"),
            UTF_8);
    Parent actual =
            (Parent) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, Parent.newBuilder());
    Parent expected = Parent.newBuilder()
            .setChild(Parent.Child.newBuilder().setName("foo").build())
            .build();
    assertEquals(expected, actual);
  }

  @Test
  public void testReadPrototxtWithMapType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.write(testPath, ImmutableList.of(
            "map_field {",
            "key: \"foo\"",
            "value: 123",
            "}"),
            UTF_8);
    MapMessage actual =
            (MapMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, MapMessage.newBuilder());
    MapMessage expected = MapMessage.newBuilder()
            .putMapField("foo", 123)
            .build();
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testReadPrototxtUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readPrototxtUnchecked("", EnumMessage.newBuilder());
  }

  @Test
  public void testWritePrototxtWithScalarTypes() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    ScalarTypesMessage message = ScalarTypesMessage.newBuilder()
            .setDoubleField(123.4)
            .setFloatField(432.1f)
            .setInt32Field(123)
            .setInt64Field(321L)
            .setBoolField(true)
            .setStringField("foo")
            .setBytesField(ByteString.copyFrom("foo".getBytes()))
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual =
            ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    assertEquals(ImmutableList.of(
            "double_field: 123.4",
            "float_field: 432.1",
            "int32_field: 123",
            "int64_field: 321",
            "bool_field: true",
            "string_field: \"foo\"",
            "bytes_field: \"foo\""), actual);
  }

  @Test
  public void testWritePrototxtWithEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage message = EnumMessage.newBuilder()
            .setFavorite(EnumMessage.Color.GREEN)
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual =
            ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    assertEquals(ImmutableList.of("favorite: GREEN"), actual);
  }

  @Test
  public void testWritePrototxtWithRepeatedEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage message = EnumMessage.newBuilder()
            .addAllAdditional(ImmutableList.of(EnumMessage.Color.BLUE, EnumMessage.Color.RED))
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual =
            ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    assertEquals(ImmutableList.of("additional: BLUE", "additional: RED"), actual);
  }

  @Test
  public void testWritePrototxtWithNestedType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Parent message = Parent.newBuilder()
            .setChild(Parent.Child.newBuilder().setName("foo").build())
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual =
            ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    assertEquals(ImmutableList.of(
            "child {",
            "  name: \"foo\"",
            "}"), actual);
  }

  @Test
  public void testWritePrototxtWithMapType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    MapMessage message = MapMessage.newBuilder()
            .putMapField("foo", 123)
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual =
            ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    assertEquals(ImmutableList.of(
            "map_field {",
            "  key: \"foo\"",
            "  value: 123",
            "}"), actual);
  }

  @Test(expected = RuntimeException.class)
  public void testWritePrototxtUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.writePrototxtUnchecked(EnumMessage.newBuilder().build(), "");
  }

  @Test
  public void testReadProtoBinaryWithScalarTypes() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    ScalarTypesMessage expected = ScalarTypesMessage.newBuilder()
            .setDoubleField(123.4)
            .setFloatField(432.1f)
            .setInt32Field(123)
            .setInt64Field(321L)
            .setBoolField(true)
            .setStringField("foo")
            .setBytesField(ByteString.copyFrom("foo".getBytes()))
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    expected.writeTo(Files.newOutputStream(testPath));
    ScalarTypesMessage actual =
            (ScalarTypesMessage) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, ScalarTypesMessage.newBuilder());
    assertEquals(expected, actual);
  }

  @Test
  public void testReadProtoBinaryEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage expected = EnumMessage.newBuilder()
            .setFavorite(EnumMessage.Color.GREEN)
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    expected.writeTo(Files.newOutputStream(testPath));
    EnumMessage actual =
            (EnumMessage) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, EnumMessage.newBuilder());
    assertEquals(expected, actual);
  }

  @Test
  public void testReadProtoBinaryWithRepeatedEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage expected = EnumMessage.newBuilder()
            .addAllAdditional(ImmutableList.of(EnumMessage.Color.BLUE, EnumMessage.Color.RED))
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    expected.writeTo(Files.newOutputStream(testPath));
    EnumMessage actual =
            (EnumMessage) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, EnumMessage.newBuilder());
    assertEquals(expected, actual);
  }

  @Test
  public void testReadProtoBinaryWithNestedType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Parent expected = Parent.newBuilder()
            .setChild(Parent.Child.newBuilder().setName("foo").build())
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    expected.writeTo(Files.newOutputStream(testPath));
    Parent actual =
            (Parent) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, Parent.newBuilder());
    assertEquals(expected, actual);
  }

  @Test
  public void testReadProtoBinaryWithMapType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    MapMessage expected = MapMessage.newBuilder()
            .putMapField("foo", 123)
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    expected.writeTo(Files.newOutputStream(testPath));
    MapMessage actual =
            (MapMessage) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, MapMessage.newBuilder());
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testReadProtoBinaryUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readProtoBinaryUnchecked("", EnumMessage.newBuilder());
  }

  @Test
  public void testWriteProtoBinaryWithScalarTypes() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    ScalarTypesMessage expected = ScalarTypesMessage.newBuilder()
            .setDoubleField(123.4)
            .setFloatField(432.1f)
            .setInt32Field(123)
            .setInt64Field(321L)
            .setBoolField(true)
            .setStringField("foo")
            .setBytesField(ByteString.copyFrom("foo".getBytes()))
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    ScalarTypesMessage actual =
            ScalarTypesMessage.newBuilder()
                    .build()
                    .getParserForType()
                    .parseFrom(Files.newInputStream(testPath));
    assertEquals(expected, actual);
  }

  @Test
  public void testWriteProtoBinaryWithEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage expected = EnumMessage.newBuilder()
            .setFavorite(EnumMessage.Color.GREEN)
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    EnumMessage actual =
            EnumMessage.newBuilder()
                    .build()
                    .getParserForType()
                    .parseFrom(Files.newInputStream(testPath));
    assertEquals(expected, actual);
  }

  @Test
  public void testWriteProtoBinaryWithRepeatedEnumType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    EnumMessage expected = EnumMessage.newBuilder()
            .addAllAdditional(ImmutableList.of(EnumMessage.Color.BLUE, EnumMessage.Color.RED))
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    EnumMessage actual =
            EnumMessage.newBuilder()
                    .build()
                    .getParserForType()
                    .parseFrom(Files.newInputStream(testPath));
    assertEquals(expected, actual);
  }

  @Test
  public void testWriteProtoBinaryWithNestedType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Parent expected = Parent.newBuilder()
            .setChild(Parent.Child.newBuilder().setName("foo").build())
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    Parent actual =
            Parent.newBuilder()
                    .build()
                    .getParserForType()
                    .parseFrom(Files.newInputStream(testPath));
    assertEquals(expected, actual);
  }

  @Test
  public void testWriteProtoBinaryWithMapType() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    MapMessage expected = MapMessage.newBuilder()
            .putMapField("foo", 123)
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    MapMessage actual =
            MapMessage.newBuilder()
                    .build()
                    .getParserForType()
                    .parseFrom(Files.newInputStream(testPath));
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testWriteProtoBinaryUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.writeProtoBinaryUnchecked(EnumMessage.newBuilder().build(), "");
  }
}