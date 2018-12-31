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

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import com.google.startupos.tools.buildfilegenerator.Protos.ThirdPartyDeps;
import com.google.startupos.tools.buildfilegenerator.Protos.ThirdPartyDep;

public class ThirdPartyDepsAnalyzer {
  private FileUtils fileUtils;

  @Inject
  public ThirdPartyDepsAnalyzer(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public ThirdPartyDeps getThirdPartyDeps() throws IOException {
    ThirdPartyDeps.Builder result = ThirdPartyDeps.newBuilder();
    List<String> targets = getTargets();

    List<String> thirdPartyFolderNames = getThirdPartyFolderNames();
    for (String folderName : thirdPartyFolderNames) {
      List<String> jarClasses =
          getJavaClassesFromJar(
              fileUtils.getCurrentWorkingDirectory()
                  + String.format(
                      "/bazel-startup-os/external/%s/jar/%s.jar", folderName, folderName));
      String target = getAssociatedTarget(targets, folderName);
      result.addThirdPartyDep(
          ThirdPartyDep.newBuilder().setTarget(target).addAllJavaClass(jarClasses).build());
    }
    return result.build();
  }

  private List<String> getTargets() throws IOException {
    return runCommand(
        "bazel query \'third_party/...\'", new String[0], fileUtils.getCurrentWorkingDirectory());
  }

  private List<String> getThirdPartyFolderNames() throws IOException {
    return fileUtils
        .listContents(fileUtils.getCurrentWorkingDirectory() + "/bazel-startup-os/external")
        .stream()
        .filter(folderName -> folderName.startsWith("mvn"))
        .collect(Collectors.toList());
  }

  private List<String> getJavaClassesFromJar(String path) throws IOException {
    return runCommand("jar tf " + path, new String[0], "")
        .stream()
        .filter(item -> item.endsWith(".class"))
        .filter(item -> !item.contains("$"))
        .collect(Collectors.toList());
  }

  private String getAssociatedTarget(List<String> targets, String thirdPartyFolderName) {
    for (String target : targets) {
      // we are doing replacing to have the ability to compare third_party folder names with bazel
      // target
      if (target
          .replace("//third_party/maven/", "mvn")
          .replace("/", "_")
          .replace(":", "_")
          .equals(thirdPartyFolderName)) {
        return target;
      }
    }
    return "";
  }

  private List<String> runCommand(String command, String[] environment, String workingDirectory)
      throws IOException {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    Process process;
    if (workingDirectory == null || workingDirectory.isEmpty()) {
      process = Runtime.getRuntime().exec(command);
    } else {
      // Executes the specified string command in a separate process with the specified environment
      // and working directory.
      process = Runtime.getRuntime().exec(command, environment, new File(workingDirectory));
    }

    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = stdout.readLine()) != null) {
      result.add(line);
    }

    BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while ((line = stderr.readLine()) != null) {
      System.out.println(line);
    }
    return result.build();
  }
}

