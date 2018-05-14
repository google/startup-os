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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.startupos.common.tests.Protos.TestMessage;
import dagger.Provides;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class FileUtilsTest {

  private static final String TEST_DIR_PATH = "/root/foo";
  private static final String TEST_FILE_PATH = "/root/foo.txt";
  private static final String TEST_PROTO_BINARY_FILE_PATH = "/root/foo.pb";
  private static final String TEST_PROTOTXT_FILE_PATH = "/root/test.prototxt";

  @Parameters(name = "{1}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
        {Configuration.unix(), "Unix"},
        {Configuration.osX(), "OSX"},
        {Configuration.windows(), "Windows"}
    });
  }

  private final Configuration fileSystemConfig;
  private final String fileSystemName;
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
    fileUtils = commonComponent.getFileUtils();
  }

  @Test
  public void testFolderExistsIsTrueWhenFolder() throws IOException {
    // TODO: Add tests for Windows. Currently, jimfs says: "Jimfs does not currently support the
    // "Windows syntax for an absolute path on the current drive".
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectories(testPath);
    assertTrue(fileUtils.folderExists(TEST_DIR_PATH));
  }

  @Test
  public void testFolderExistsIsFalseWhenFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
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
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of("hello world"), UTF_8);
    assertTrue(fileUtils.fileExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileExistsIsFalseWhenFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectories(testPath);
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
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of("hello world"), UTF_8);
    assertTrue(fileUtils.fileOrFolderExists(TEST_FILE_PATH));
  }

  @Test
  public void testFileOrFolderExistsIsTrueWhenFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectories(testPath);
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
  public void testMkdirsWhenFolderNameContainsDot() {
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
    Files.createDirectories(testPath.getParent());
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
  public void testWriteStringWhenPathWithoutParentFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath("/foo.txt");
    fileUtils.writeString("hello world", "/foo.txt");
    assertTrue(Files.readAllLines(testPath, UTF_8).contains("hello world"));
  }

  @Test(expected = RuntimeException.class)
  public void testWriteStringUncheckedWithException() {
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
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of("first line"), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line", content);
  }

  @Test
  public void testReadFileWhenTwoLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of("first line", "second line"), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\nsecond line", content);
  }

  @Test
  public void testReadFileWhenIsEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of(), UTF_8);
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("", content);
  }

  @Test(expected = RuntimeException.class)
  public void testReadFileUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readFileUnchecked("");
  }

  @Test
  public void testListContentsWhenOneFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.createFile(testPath);
    ImmutableList<String> names = fileUtils.listContents("/root");
    assertEquals(ImmutableList.of("foo.txt"), names);
  }

  @Test
  public void testListContentsWhenOneFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    Files.createDirectories(testPath);
    ImmutableList<String> names = fileUtils.listContents("/root");
    assertEquals(ImmutableList.of("foo"), names);
  }

  @Test
  public void testListContentsWhenFileWithoutExtension() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath);
    Files.createFile(fileSystem.getPath(TEST_FILE_PATH + "/foo"));
    ImmutableList<String> names = fileUtils.listContents(TEST_FILE_PATH);
    assertEquals(ImmutableList.of("foo"), names);
  }

  @Test
  public void testListContentsWhenTwoFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/foo"));
    Files.createFile(fileSystem.getPath("/foo/first_file.txt"));
    Files.createFile(fileSystem.getPath("/foo/second_file.txt"));
    ImmutableList<String> names = fileUtils.listContents("/foo");
    assertEquals(ImmutableList.of("first_file.txt", "second_file.txt"), names);
  }

  @Test
  public void testListContentsWhenTwoFolders() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/foo/first_folder"));
    Files.createDirectories(fileSystem.getPath("/foo/second_folder"));
    ImmutableList<String> names = fileUtils.listContents("/foo");
    assertEquals(ImmutableList.of("first_folder", "second_folder"), names);
  }

  @Test
  public void testListContentsWhenTwoFoldersAndTwoFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/foo/first_folder"));
    Files.createDirectories(fileSystem.getPath("/foo/second_folder"));
    Files.createFile(fileSystem.getPath("/foo/first_file.txt"));
    Files.createFile(fileSystem.getPath("/foo/second_file.txt"));
    ImmutableList<String> names = fileUtils.listContents("/foo");
    assertEquals(ImmutableList.of("first_file.txt", "first_folder", "second_file.txt", "second_folder"), names);
  }

  @Test
  public void testListContentsWhenPathDoesNotContainFilesAndFolders() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    ImmutableList<String> names = fileUtils.listContents(TEST_DIR_PATH);
    assertEquals(ImmutableList.of(), names);
  }

  @Test
  public void testListContentsWhenFileInSubdirectory() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/subdirectory"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/subdirectory" + "/foo.txt"));
    ImmutableList<String> names = fileUtils.listContents(TEST_DIR_PATH);
    assertEquals(ImmutableList.of("subdirectory"), names);
  }

  @Test
  public void testListContentsRecursively() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/empty_folder"));
    Files.createDirectories(fileSystem.getPath("/path/to/folder"));
    Files.createFile(fileSystem.getPath("/first_file.txt"));
    Files.createFile(fileSystem.getPath("/path/to/folder/second_file.txt"));
    Files.createFile(fileSystem.getPath("/path/to/folder/third_file.txt"));
    ImmutableList<String> paths = fileUtils.listContentsRecursively("/");
    assertEquals(
        ImmutableList.of(
            "/",
            "/empty_folder",
            "/first_file.txt",
            "/path",
            "/path/to",
            "/path/to/folder",
            "/path/to/folder/second_file.txt",
            "/path/to/folder/third_file.txt",
            "/work"),
        paths);
  }

  @Test
  public void testListContentsRecursivelyWhenEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    ImmutableList<String> paths = fileUtils.listContentsRecursively("/");
    assertEquals(
        ImmutableList.of(
            "/",
            "/work"),
        paths);
  }

  @Test(expected = NoSuchFileException.class)
  public void testListContentsRecursivelyWhenDirectoryNotExists() throws IOException {
    if (fileSystemName.equals("Windows")) {
      throw new NoSuchFileException("");
    }
    ImmutableList<String> paths = fileUtils.listContentsRecursively(TEST_DIR_PATH);
    assertEquals(ImmutableList.of(), paths);
  }

  @Test
  public void testReadPrototxt() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_PROTOTXT_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, ImmutableList.of(
        "int32_field: 123",
        "string_field: \"foo\"",
        "map_field {",
        "key: \"foo\"",
        "value: 123",
        "}",
        "enum_field: YES"),
        UTF_8);
    TestMessage actual = (TestMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, TestMessage.newBuilder());
    TestMessage expected = TestMessage.newBuilder()
        .setInt32Field(123)
        .setStringField("foo")
        .putMapField("foo", 123)
        .setEnumField(TestMessage.BooleanEnum.YES)
        .build();
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testReadPrototxtUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readPrototxtUnchecked("", TestMessage.newBuilder());
  }

  @Test
  public void testWritePrototxt() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    TestMessage message = TestMessage.newBuilder()
        .setInt32Field(123)
        .setStringField("foo")
        .putMapField("foo", 123)
        .setEnumField(TestMessage.BooleanEnum.YES)
        .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    ImmutableList<String> actual = ImmutableList.copyOf(Files.readAllLines(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)));
    ImmutableList<String> expected = ImmutableList.of(
        "int32_field: 123",
        "string_field: \"foo\"",
        "map_field {",
        "  key: \"foo\"",
        "  value: 123",
        "}",
        "enum_field: YES");
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testWritePrototxtUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.writePrototxtUnchecked(TestMessage.newBuilder().build(), "");
  }

  @Test
  public void testReadProtoBinary() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    TestMessage expected = TestMessage.newBuilder()
        .setInt32Field(123)
        .setStringField("foo")
        .putMapField("foo", 123)
        .setEnumField(TestMessage.BooleanEnum.YES)
        .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    expected.writeTo(Files.newOutputStream(testPath));
    TestMessage actual = (TestMessage) fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, TestMessage.newBuilder());
    assertEquals(expected, actual);
  }


  @Test(expected = RuntimeException.class)
  public void testReadProtoBinaryUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.readProtoBinaryUnchecked("", TestMessage.newBuilder());
  }

  @Test
  public void testWriteProtoBinary() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    TestMessage expected = TestMessage.newBuilder()
        .setInt32Field(123)
        .setStringField("foo")
        .putMapField("foo", 123)
        .setEnumField(TestMessage.BooleanEnum.YES)
        .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    TestMessage actual = TestMessage.newBuilder()
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
    fileUtils.writeProtoBinaryUnchecked(TestMessage.newBuilder().build(), "");
  }
}
