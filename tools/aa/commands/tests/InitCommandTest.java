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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.aa.AaModule;
import com.google.startupos.tools.aa.commands.AaCommand;
import com.google.startupos.tools.aa.commands.InitCommand;
import dagger.Component;
import dagger.Provides;
import java.nio.file.FileSystem;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;

public class InitCommandTest {

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  interface TestComponent {
    InitCommand getCommand();

    FileUtils getFileUtils();
  }

  private AaCommand initCommand;
  private FileUtils fileUtils;

  @Before
  public void setup() {
    // XXX parametrize test
    FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    TestComponent component =
        DaggerInitCommandTest_TestComponent.builder()
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
    initCommand = component.getCommand();
    fileUtils = component.getFileUtils();
  }

  @Test
  public void initCommandTest() throws Exception {
    String[] args = {
      "--base_path", "/path/to/base",
      "--startupos_repo", ""
    };
    initCommand.run(args);
    ImmutableList<String> paths = fileUtils.listContentsRecursively("/");
    assertEquals(
        ImmutableList.of(
            "/",
            "/path",
            "/path/to",
            "/path/to/base",
            "/path/to/base/BASE",
            "/path/to/base/head",
            "/path/to/base/local",
            "/path/to/base/logs",
            "/path/to/base/ws",
            "/work"),
        paths);
  }
}

