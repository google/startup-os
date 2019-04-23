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

package com.google.startupos.tools.build_file_generator;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;

// TODO: Create rules that are not specific to StartupOS, and that we don't need to change if the
// code will be changed.
public class BuildFileGeneratorTool {
  @FlagDesc(
      name = "build_file_generation_blacklist",
      description = "The packages where is not need to create BUILD files")
  private static Flag<List<String>> buildFileGenerationBlacklist =
      Flag.createStringsListFlag(
          Arrays.asList(
              // A `resources` folder can contain proto files. If the `resources` folder contains a
              // BUILD file, it impossible to use proto files as a label in a `resources` argument.
              "resources",
              // It's a subfolder. BUILD file from them is in the parent folder.
              "tools/reviewer/aa/commands/checks",
              // Currently, we don't support auto-generating BUIlD files for android projects
              "examples/android/activities"));

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    BuildFileGeneratorToolComponent component =
        DaggerBuildFileGeneratorTool_BuildFileGeneratorToolComponent.create();

    component
        .getBuildFileGenerator()
        .generateBuildFiles(buildFileGenerationBlacklist.get())
        .forEach((path, buildFile) -> component.getBuildFileWriter().write(buildFile, path));
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface BuildFileGeneratorToolComponent {
    FileUtils getFileUtils();

    BuildFileGenerator getBuildFileGenerator();

    BuildFileWriter getBuildFileWriter();
  }
}

