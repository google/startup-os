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
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

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
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class FileUtilsTest {

  private static final String TEST_DIR_PATH = "/root/foo";
  private static final String TEST_FILE_PATH = "/root/foo.txt";
  private static final String TEST_PROTO_BINARY_FILE_PATH = "/root/foo.pb";
  private static final String TEST_PROTOTXT_FILE_PATH = "/root/test.prototxt";

  @Parameters(name = "{1}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(
        new Object[][] {
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
    CommonComponent commonComponent =
        DaggerCommonComponent.builder()
            .commonModule(
                new CommonModule() {
                  @Provides
                  @Singleton
                  @Override
                  public FileSystem provideDefaultFileSystem() {
                    return fileSystem;
                  }
                })
            .build();
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
    Files.write(testPath, "hello world".getBytes(UTF_8));
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
    Files.write(testPath, "hello world".getBytes(UTF_8));
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
    Files.write(testPath, "hello world".getBytes(UTF_8));
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
    assertEquals("hello world", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringOneLineWithNewLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    fileUtils.writeString("hello world\n", TEST_FILE_PATH);
    assertEquals("hello world\n", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringOneLineWithTwoNewLinesInTheEnd() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    fileUtils.writeString("hello world\n\n", TEST_FILE_PATH);
    assertEquals("hello world\n\n", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringTwoLinesWithNewLines() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    fileUtils.writeString("first line\nsecond line\n", TEST_FILE_PATH);
    assertEquals("first line\nsecond line\n", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringWhenFileIsNotEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "first line".getBytes(UTF_8));
    fileUtils.writeString("second line", TEST_FILE_PATH);
    assertEquals("second line", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringWhenFileWithoutExtension() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_DIR_PATH);
    fileUtils.writeString("hello world", TEST_DIR_PATH);
    assertEquals("hello world", new String(Files.readAllBytes(testPath), UTF_8));
  }

  @Test
  public void testWriteStringWhenPathWithoutParentFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath("/foo.txt");
    fileUtils.writeString("hello world", "/foo.txt");
    assertEquals("hello world", new String(Files.readAllBytes(testPath), UTF_8));
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
    Files.write(testPath, "first line".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line", content);
  }

  @Test
  public void testReadFileWhenOneLineWithNewLineInTheEnd() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "first line\n".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\n", content);
  }

  @Test
  public void testReadFileWhenOneLineWithTwoNewLinesInTheEnd() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "first line\n\n".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\n\n", content);
  }

  @Test
  public void testReadFileWhenTwoLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "first line\nsecond line".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\nsecond line", content);
  }

  @Test
  public void testReadFileWhenTwoLineWithNewLineInTheEnd() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "first line\nsecond line\n".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("first line\nsecond line\n", content);
  }

  @Test
  public void testReadFileWhenIsEmpty() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("", content);
  }

  @Test
  public void testReadFileWhenIsEmptyWithNewLine() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Path testPath = fileSystem.getPath(TEST_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    Files.write(testPath, "\n".getBytes(UTF_8));
    String content = fileUtils.readFile(TEST_FILE_PATH);
    assertEquals("\n", content);
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
    assertEquals(
        ImmutableList.of("first_file.txt", "first_folder", "second_file.txt", "second_folder"),
        names);
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
    assertEquals(ImmutableList.of("/", "/work"), paths);
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
    Files.write(
        testPath,
        ("int32_field: 123\n"
                + "string_field: \"foo\"\n"
                + "map_field {\n"
                + "key: \"foo\"\n"
                + "value: 123\n"
                + "}\n"
                + "enum_field: YES")
            .getBytes(UTF_8));
    TestMessage actual =
        (TestMessage) fileUtils.readPrototxt(TEST_PROTOTXT_FILE_PATH, TestMessage.newBuilder());
    TestMessage expected =
        TestMessage.newBuilder()
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
    TestMessage message =
        TestMessage.newBuilder()
            .setInt32Field(123)
            .setStringField("foo")
            .putMapField("foo", 123)
            .setEnumField(TestMessage.BooleanEnum.YES)
            .build();
    fileUtils.writePrototxt(message, TEST_PROTOTXT_FILE_PATH);
    String actual =
        new String(Files.readAllBytes(fileSystem.getPath(TEST_PROTOTXT_FILE_PATH)), UTF_8);
    String expected =
        "int32_field: 123\n"
            + "string_field: \"foo\"\n"
            + "map_field {\n"
            + "  key: \"foo\"\n"
            + "  value: 123\n"
            + "}\n"
            + "enum_field: YES\n";
    assertEquals(expected, actual);
  }

  @Test(expected = RuntimeException.class)
  public void testWritePrototxtUncheckedWithException() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.writePrototxtUnchecked(TestMessage.getDefaultInstance(), "");
  }

  @Test
  public void testReadProtoBinary() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    TestMessage expected =
        TestMessage.newBuilder()
            .setInt32Field(123)
            .setStringField("foo")
            .putMapField("foo", 123)
            .setEnumField(TestMessage.BooleanEnum.YES)
            .build();
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    Files.createDirectories(testPath.getParent());
    expected.writeTo(Files.newOutputStream(testPath));
    TestMessage actual =
        (TestMessage)
            fileUtils.readProtoBinary(TEST_PROTO_BINARY_FILE_PATH, TestMessage.newBuilder());
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
    TestMessage expected =
        TestMessage.newBuilder()
            .setInt32Field(123)
            .setStringField("foo")
            .putMapField("foo", 123)
            .setEnumField(TestMessage.BooleanEnum.YES)
            .build();
    fileUtils.writeProtoBinary(expected, TEST_PROTO_BINARY_FILE_PATH);
    Path testPath = fileSystem.getPath(TEST_PROTO_BINARY_FILE_PATH);
    TestMessage actual =
        TestMessage.newBuilder()
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
    fileUtils.writeProtoBinaryUnchecked(TestMessage.getDefaultInstance(), "");
  }

  @Test
  public void testDeleteDirectory() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/foo/empty_folder"));
    Files.createDirectories(fileSystem.getPath("/foo/path/to/folder"));
    Files.createFile(fileSystem.getPath("/foo/first_file.txt"));
    Files.createFile(fileSystem.getPath("/foo/path/to/folder/second_file.txt"));
    Files.createFile(fileSystem.getPath("/foo/path/to/folder/third_file.txt"));
    fileUtils.deleteDirectory("/foo");
    assertFalse(Files.isDirectory(fileSystem.getPath("/foo/empty_folder")));
    assertFalse(Files.isDirectory(fileSystem.getPath("/foo/path/to/folder")));
    assertFalse(Files.isRegularFile(fileSystem.getPath("/foo/first_file.txt")));
    assertFalse(Files.isRegularFile(fileSystem.getPath("/foo/path/to/folder/second_file.txt")));
    assertFalse(Files.isRegularFile(fileSystem.getPath("/foo/path/to/folder/third_file.txt")));
    assertFalse(Files.isDirectory(fileSystem.getPath("/foo")));
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteDirectoryUnchecked() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.deleteDirectoryUnchecked("");
  }

  @Test
  public void testDeleteFileOrDirectoryIfExists() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath("/foo/folder"));
    Files.createFile(fileSystem.getPath("/foo/file.txt"));
    fileUtils.deleteFileOrDirectoryIfExists("/foo/folder");
    fileUtils.deleteFileOrDirectoryIfExists("/foo/file.txt");
    assertFalse(Files.isDirectory(fileSystem.getPath("/foo/folder")));
    assertFalse(Files.isRegularFile(fileSystem.getPath("/foo/file.txt")));
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteFileOrDirectoryIfExistsUnchecked() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.deleteFileOrDirectoryIfExistsUnchecked("");
  }

  @Test
  public void testClearDirectoryWhenFolders() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/first_folder"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder"));
    fileUtils.clearDirectory(TEST_DIR_PATH);
    assertFalse(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH + "/first_folder")));
    assertFalse(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder")));
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH)));
  }

  @Test
  public void testClearDirectoryWhenFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/first_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/second_file.txt"));
    fileUtils.clearDirectory(TEST_DIR_PATH);
    assertFalse(Files.isRegularFile(fileSystem.getPath(TEST_DIR_PATH + "/first_file.txt")));
    assertFalse(Files.isRegularFile(fileSystem.getPath(TEST_DIR_PATH + "/second_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH)));
  }

  @Test
  public void testClearDirectoryWhenFilesAndFolders() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/empty_folder"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/first_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder/second_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder/third_file.txt"));
    fileUtils.clearDirectory(TEST_DIR_PATH);
    assertFalse(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH + "/empty_folder")));
    assertFalse(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder")));
    assertFalse(Files.isRegularFile(fileSystem.getPath(TEST_DIR_PATH + "/first_file.txt")));
    assertFalse(
        Files.isRegularFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder/second_file.txt")));
    assertFalse(
        Files.isRegularFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder/third_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath(TEST_DIR_PATH)));
  }

  @Test(expected = RuntimeException.class)
  public void testClearDirectoryUnchecked() {
    if (fileSystemName.equals("Windows")) {
      throw new RuntimeException();
    }
    fileUtils.clearDirectoryUnchecked("/nonexistent_path");
  }

  @Test
  public void testCopyDirectoryToDirectoryWithoutIgnored() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/file1.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/file2.txt"));

    fileUtils.copyDirectoryToDirectory(TEST_DIR_PATH, "destination_folder");

    assertTrue(Files.isRegularFile(fileSystem.getPath("destination_folder" + "/file1.txt")));
    assertTrue(
        Files.isRegularFile(fileSystem.getPath("destination_folder" + "/path/to/file2.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredOneFile() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/file_for_ignore.txt"));

    fileUtils.copyDirectoryToDirectory(TEST_DIR_PATH, "destination_folder", "file_for_ignore.txt");

    assertTrue(Files.isRegularFile(fileSystem.getPath("destination_folder" + "/some_file.txt")));
    assertFalse(
        Files.isRegularFile(fileSystem.getPath("destination_folder" + "/file_for_ignore.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredTwoFiles() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/file_for_ignore1.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/file_for_ignore2.txt"));

    fileUtils.copyDirectoryToDirectory(
        TEST_DIR_PATH,
        "destination_folder",
        "file_for_ignore1.txt",
        "path/to/file_for_ignore2.txt");

    assertTrue(Files.isRegularFile(fileSystem.getPath("destination_folder" + "/some_file.txt")));
    assertFalse(
        Files.isRegularFile(fileSystem.getPath("destination_folder" + "/file_for_ignore1.txt")));
    assertFalse(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "path/to/file_for_ignore2.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredOneFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/some_folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_folder/some_file.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore"));
    Files.createDirectories(
        fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore/internal_folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore/file2.txt"));

    fileUtils.copyDirectoryToDirectory(
        TEST_DIR_PATH, "destination_folder", "path/to/folder_for_ignore");

    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/some_folder")));
    assertTrue(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/some_folder/some_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to")));
    assertFalse(
        Files.isDirectory(
            fileSystem.getPath(
                "destination_folder" + "/path/to/folder_for_ignore/internal_folder")));
    assertFalse(
        Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore")));
    assertFalse(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore/file2.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredTwoFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/some_folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_folder/some_file.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/folder_for_ignore1"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/folder_for_ignore1/file1.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore2"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore2/file2.txt"));

    fileUtils.copyDirectoryToDirectory(
        TEST_DIR_PATH, "destination_folder", "folder_for_ignore1", "path/to/folder_for_ignore2");

    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/some_folder")));
    assertTrue(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/some_folder/some_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to")));
    assertFalse(
        Files.isDirectory(fileSystem.getPath("destination_folder" + "/folder_for_ignore1")));
    assertFalse(
        Files.isDirectory(
            fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore2")));
    assertFalse(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore2/file2.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredTwoFolderWithRegex() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/some_folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_folder/some_file.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/folder_for_ignore"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/folder_for_ignore/file1.txt"));
    Files.createDirectories(
        fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore_with_regex"));
    Files.createFile(
        fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore_with_regex/file2.txt"));

    fileUtils.copyDirectoryToDirectory(TEST_DIR_PATH, "destination_folder", "folder_for_ignore.*");

    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/some_folder")));
    assertTrue(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/some_folder/some_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to")));
    assertFalse(Files.isDirectory(fileSystem.getPath("destination_folder" + "/folder_for_ignore")));
    assertFalse(
        Files.isDirectory(
            fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore_with_regex")));
    assertFalse(
        Files.isRegularFile(
            fileSystem.getPath(
                "destination_folder" + "/path/to/folder_for_ignore_with_regex/file2.txt")));
  }

  @Test
  public void testCopyDirectoryToDirectoryWhenIgnoredFileAndFolder() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/some_folder"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/some_folder/some_file.txt"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/file1.txt"));
    Files.createDirectories(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore"));
    Files.createFile(fileSystem.getPath(TEST_DIR_PATH + "/path/to/folder_for_ignore/file2.txt"));

    fileUtils.copyDirectoryToDirectory(
        TEST_DIR_PATH, "destination_folder", "file1.txt", "path/to/folder_for_ignore");

    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/some_folder")));
    assertTrue(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/some_folder/some_file.txt")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path")));
    assertTrue(Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to")));
    assertFalse(Files.isRegularFile(fileSystem.getPath("destination_folder" + "/file1.txt")));
    assertFalse(
        Files.isDirectory(fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore")));
    assertFalse(
        Files.isRegularFile(
            fileSystem.getPath("destination_folder" + "/path/to/folder_for_ignore/file2.txt")));
  }
}

