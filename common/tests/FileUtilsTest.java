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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.google.common.collect.ImmutableList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Path;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import dagger.Provides;
import javax.inject.Singleton;
import java.util.Collection;
import java.io.IOException;


@RunWith(Parameterized.class)
public class FileUtilsTest {
  @Parameters(name="{1}")
  public static Collection<Object[]> parameters() {
    return Arrays.asList(new Object[][] {
      {Configuration.unix(), "Unix"},
      {Configuration.osX(), "OSX"},
      {Configuration.windows(), "Windows"}
    });
  }
  static String TEST_DIR_PATH = "/foo";
  static String TEST_FILE_PATH = "/foo.txt";

  Configuration fileSystemConfig;
  String fileSystemName;
  FileSystem fileSystem;
  FileUtils fileUtils;

  public FileUtilsTest(Configuration fileSystemConfig, String fileSystemName) {
    this.fileSystemConfig = fileSystemConfig;
    this.fileSystemName = fileSystemName;
  }

  @Before
  public void setup() throws Exception {
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
  public void testFolderExistsIsFalseWhenNothing() throws IOException {
    if (fileSystemName.equals("Windows")) {
      return;
    }    
    assertFalse(fileUtils.folderExists(TEST_FILE_PATH));
  }
}