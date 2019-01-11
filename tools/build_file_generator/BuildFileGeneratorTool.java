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
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile;
import dagger.Component;

import java.io.IOException;
import javax.inject.Singleton;

public class BuildFileGeneratorTool {
  @FlagDesc(name = "path", description = "Package path to generate BUILD file")
  private static Flag<String> path = Flag.create("tools/build_file_generator");

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    BuildFileGeneratorToolComponent component =
        DaggerBuildFileGeneratorTool_BuildFileGeneratorToolComponent.create();

    FileUtils fileUtils = component.getFileUtils();
    String packagePath = fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), path.get());
    BuildFile buildFile = component.getBuildFileGenerator().generateBuildFile(packagePath);
    component.getBuildFileWriter().write(buildFile, packagePath);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface BuildFileGeneratorToolComponent {
    FileUtils getFileUtils();

    BuildFileGenerator getBuildFileGenerator();

    BuildFileWriter getBuildFileWriter();
  }
}

