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

package com.google.startupos.tools.build_file_generator.http_archive_deps;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDeps;
import com.google.startupos.tools.build_file_generator.Protos.WorkspaceFile;
import dagger.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;

public class HttpArchiveDepsTool {
  private static final String HTTP_ARCHIVE_DEPS_FILENAME = "http_archive_deps.prototxt";

  @FlagDesc(name = "http_archive_names", description = "http_archive names to process")
  static final Flag<List<String>> httpArchiveNames =
      Flag.createStringsListFlag(Collections.singletonList("startup_os"));

  public void write(FileUtils fileUtils, HttpArchiveDeps httpArchiveDeps, String absPath) {
    fileUtils.writePrototxtUnchecked(httpArchiveDeps, absPath);
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    HttpArchiveDepsTool.HttpArchiveDepsToolComponent component =
        DaggerHttpArchiveDepsTool_HttpArchiveDepsToolComponent.create();

    FileUtils fileUtils = component.getFileUtils();
    WorkspaceFile workspaceFile = component.getWorkspaceParser().getWorkspaceFile();
    HttpArchiveDeps httpArchiveDeps =
        component
            .getHttpArchiveDepsGenerator()
            .getHttpArchiveDeps(workspaceFile, httpArchiveNames.get());
    new HttpArchiveDepsTool()
        .write(
            fileUtils,
            httpArchiveDeps,
            fileUtils.joinPaths(
                fileUtils.getCurrentWorkingDirectory(), HTTP_ARCHIVE_DEPS_FILENAME));
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface HttpArchiveDepsToolComponent {
    FileUtils getFileUtils();

    WorkspaceParser getWorkspaceParser();

    HttpArchiveDepsGenerator getHttpArchiveDepsGenerator();
  }
}

