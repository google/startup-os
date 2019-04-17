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

package com.google.startupos.tools.build_file_generator.third_party_deps;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.Component;

import java.io.IOException;
import javax.inject.Singleton;

/* A tool for generating the zip-archive with prototxt file inside
 * to know which classes each third_party dependency includes.
 * Path to the zip-archive: "<project_root>/third_party_deps.zip"
 *
 * Usage:
 * bazel run //tools/build_file_generator/third_party_deps:third_party_deps_tool --
 * --build_file_path <path/to/BUILD/file>
 */
public class ThirdPartyDepsTool {
  private static final String PATH_TO_ZIP = "third_party_deps.zip";
  private static final String PROTOTXT_FILENAME_INSIDE_ZIP = "third_party_deps.prototxt";

  @FlagDesc(
      name = "build_file_path",
      description =
          "Absolute path to BUILD file with third party deps. "
              + "The file usually located by path: "
              + "<path/to/repo>/tools/build_file_generator/third_party_deps/BUILD")
  static Flag<String> buildFilePath = Flag.create("");

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    ThirdPartyDepsToolComponent component =
        DaggerThirdPartyDepsTool_ThirdPartyDepsToolComponent.create();

    FileUtils fileUtils = component.getFileUtils();
    fileUtils.writePrototxtToZip(
        component.getThirdPartyDepsAnalyzer().getThirdPartyDeps(buildFilePath.get()),
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), PATH_TO_ZIP),
        PROTOTXT_FILENAME_INSIDE_ZIP);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ThirdPartyDepsToolComponent {
    FileUtils getFileUtils();

    ThirdPartyDepsAnalyzer getThirdPartyDepsAnalyzer();
  }
}

