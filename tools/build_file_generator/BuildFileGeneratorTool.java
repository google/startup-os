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
import dagger.Component;

import java.io.IOException;
import javax.inject.Singleton;

// TODO: Create rules that are not specific to StartupOS, and that we don't need to change if the
// code will be changed.
public class BuildFileGeneratorTool {
  public static void main(String[] args) throws IOException {
    BuildFileGeneratorToolComponent component =
        DaggerBuildFileGeneratorTool_BuildFileGeneratorToolComponent.create();

    component
        .getBuildFileGenerator()
        .generateBuildFiles()
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

