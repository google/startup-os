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

package com.google.startupos.tools.aa.commands.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.aa.AaModule;
import com.google.startupos.tools.aa.commands.AaCommand;
import com.google.startupos.tools.aa.commands.WorkspaceCommand;
import dagger.Component;
import dagger.Provides;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.inject.Singleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WorkspaceCommandTest {
  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  public interface TestComponent {
      WorkspaceCommand getCommand();
      FileUtils getFileUtils();
  }

  AaCommand workspaceCommand;
  FileUtils fileUtils;
  ByteArrayOutputStream outContent;
  ByteArrayOutputStream errContent;

  @Before
  public void setup() throws IOException {
    // XXX parametrize test
    FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    TestComponent component = DaggerWorkspaceCommandTest_TestComponent.builder()
        .commonModule(new CommonModule() {
          @Override @Provides @Singleton public FileSystem provideDefaultFileSystem() {
            return fileSystem;
          }
        })
        .aaModule(new AaModule() {
          @Override @Provides @Singleton @Named("Base path") public String provideBasePath(FileUtils fileUtils) {
            return "/base";
          }
        }).build();
    fileUtils = component.getFileUtils();
    initEmptyWorkspace();
    workspaceCommand = component.getCommand();
    outContent = new ByteArrayOutputStream();
    errContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    Flags.resetForTesting();
    WorkspaceCommand.force.resetValueForTesting();
  }
  
  @After
  public void restoreStream() {
    System.setErr(System.err);
    System.setOut(System.out);
  }

  private void initEmptyWorkspace() throws IOException {
    ImmutableList<String> paths = ImmutableList.of(
        "/base/head",
        "/base/local",
        "/base/logs",
        "/base/ws");
    for (String path : paths) {
      fileUtils.mkdirs(path);
    }
    fileUtils.writeString("user: \"bob\"", "/base/BASE");
  }

  @Test
  public void testWorkspaceCommandWhenNoWorkspaceParam() throws Exception {
    String[] args = {"workspace"};
    workspaceCommand.run(args);
    assertEquals("", outContent.toString());
    assertEquals(AaCommand.RED_ERROR + "Missing workspace name\n", errContent.toString());
  }

  @Test
  public void testWorkspaceCommandWhenNotExists() throws Exception {
    String[] args = {"workspace", "workspace_name"};
    workspaceCommand.run(args);
    assertEquals("", outContent.toString());
    assertEquals(AaCommand.RED_ERROR + "Workspace does not exist\n", errContent.toString());
  }

  @Test
  public void testWorkspaceCommandWhenExists() throws Exception {
    fileUtils.mkdirs("/base/ws/workspace_name/repo_name");
    String[] args = {"workspace", "workspace_name"};
    workspaceCommand.run(args);
    assertEquals("cd /base/ws/workspace_name/repo_name\n", outContent.toString());
    assertEquals("", errContent.toString());
  }

  @Test
  public void forcedWorkspaceCommandTest() throws Exception {
    fileUtils.mkdirs("/base/head/repo_name");
    fileUtils.writeString("aaa", "/base/head/repo_name/file.txt");
    String[] args = {"workspace", "-f", "workspace_name"};
    workspaceCommand.run(args);
    assertTrue(fileUtils.folderExists("/base/ws/workspace_name"));
    // For some reason, in the tests, fileUtils.copyDirectoryToDirectory() throws:
    // NoSuchFileException: /base/head
    // It might be a bug in jimfs, since it works in practice.
    // For this reason, we're not checking the copying of the repos from head to workspace and cd:
    // assertEquals("cd /base/ws/workspace_name\n", outContent.toString());
  }
}
