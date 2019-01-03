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

package com.google.startupos.tools.buildfilegenerator;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.buildfilegenerator.Protos.ThirdPartyDeps;
import dagger.Component;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/* A tool for generating the zip-archive with prototxt file inside
 * to know which classes each third_party dependency includes.
 * Path to the zip-archive: "//tools/build_file_generator/third_party_deps.zip"
 *
 * Usage:
 * bazel run //tools/build_file_generator:third_party_deps_tool
 */
public class ThirdPartyDepsTool {
  public static void main(String[] args) throws IOException {
    ThirdPartyDepsToolComponent component =
        DaggerThirdPartyDepsTool_ThirdPartyDepsToolComponent.create();

    createZip(component.getFileUtils(), component.getThirdPartyDepsAnalyzer().getThirdPartyDeps());
  }

  private static void createZip(FileUtils fileUtils, ThirdPartyDeps thirdPartyDeps)
      throws IOException {
    final String prototxtFile = "third_party_deps.prototxt";
    String absBuildFileGeneratorDirPath =
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "tools/build_file_generator");

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");

    URI uri =
        URI.create(
            String.format(
                "jar:file:%s",
                fileUtils.joinPaths(absBuildFileGeneratorDirPath, "third_party_deps.zip")));
    try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
      fileUtils.writePrototxtUnchecked(
          thirdPartyDeps, fileUtils.joinPaths(absBuildFileGeneratorDirPath, prototxtFile));
      Path prototxtFilePath =
          Paths.get(fileUtils.joinPaths(absBuildFileGeneratorDirPath, prototxtFile));
      Files.copy(
          prototxtFilePath, zipfs.getPath("/" + prototxtFile), StandardCopyOption.REPLACE_EXISTING);
    } finally {
      fileUtils.deleteFileOrDirectoryIfExists(
          fileUtils.joinPaths(absBuildFileGeneratorDirPath, prototxtFile));
    }
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ThirdPartyDepsToolComponent {
    FileUtils getFileUtils();

    ThirdPartyDepsAnalyzer getThirdPartyDepsAnalyzer();
  }
}

