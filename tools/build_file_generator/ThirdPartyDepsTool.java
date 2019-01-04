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
import dagger.Component;

import javax.inject.Singleton;
import java.io.IOException;

/* A tool for generating the zip-archive with prototxt file inside
 * to know which classes each third_party dependency includes.
 * Path to the zip-archive: "tools/build_file_generator/third_party_deps.zip"
 *
 * Usage:
 * bazel run //tools/build_file_generator:third_party_deps_tool
 */
public class ThirdPartyDepsTool {
  public static void main(String[] args) throws IOException {
    ThirdPartyDepsToolComponent component =
        DaggerThirdPartyDepsTool_ThirdPartyDepsToolComponent.create();

    FileUtils fileUtils = component.getFileUtils();
    component
        .getFileUtils()
        .writePrototxtToZip(
            component.getThirdPartyDepsAnalyzer().getThirdPartyDeps(),
            fileUtils.joinPaths(
                fileUtils.getCurrentWorkingDirectory(),
                "tools/build_file_generator/third_party_deps.zip"),
            "third_party_deps.prototxt");
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ThirdPartyDepsToolComponent {
    FileUtils getFileUtils();

    ThirdPartyDepsAnalyzer getThirdPartyDepsAnalyzer();
  }
}

