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

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.FileUtils;

import com.google.startupos.tools.build_file_generator.BuildFileParser;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDep;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDeps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class ThirdPartyDepsAnalyzer {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String THIRD_PARTY_DEPS_TOOL_JAVA_BINARY_NAME = "third_party_deps_tool";
  private FileUtils fileUtils;
  private BuildFileParser buildFileParser;

  @Inject
  public ThirdPartyDepsAnalyzer(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
    buildFileParser = new BuildFileParser(fileUtils);
  }

  public ThirdPartyDeps getThirdPartyDeps() throws IOException {
    final String repoName =
        fileUtils
            .getCurrentWorkingDirectory()
            .substring(fileUtils.getCurrentWorkingDirectory().lastIndexOf('/') + 1);
    ThirdPartyDeps.Builder result = ThirdPartyDeps.newBuilder();
    for (String target : getThirdPartyTargets(repoName)) {
      String folderName = getFolderNameByTarget(getThirdPartyFolderNames(repoName), target);
      if (!folderName.isEmpty()) {
        List<String> jarClasses =
            getJavaClassesFromJar(
                fileUtils.getCurrentWorkingDirectory()
                    + String.format(
                        "/bazel-%s/external/%s/jar/%s.jar", repoName, folderName, folderName));
        result.addThirdPartyDep(
            ThirdPartyDep.newBuilder().setTarget(target).addAllJavaClass(jarClasses).build());
      }
    }
    return result.build();
  }

  private ImmutableList<String> getThirdPartyTargets(String repoName) {
    final String rootPackageName = repoName.replace("-", "");
    final String absBuildFilePath =
        fileUtils.joinPaths(
            fileUtils.getCurrentWorkingDirectory(),
            this.getClass()
                .getPackage()
                .getName()
                .split(rootPackageName + ".")[1]
                .replace(".", "/"),
            "BUILD");
    BuildFile buildFile = buildFileParser.getBuildFile(absBuildFilePath);
    if (buildFile.getJavaBinaryCount() == 0) {
      log.atWarning().log("%s file doesn't contain any java_binaries", absBuildFilePath);
    } else {
      for (BuildFile.JavaBinary javaBinary : buildFile.getJavaBinaryList()) {
        if (javaBinary.getName().equals(THIRD_PARTY_DEPS_TOOL_JAVA_BINARY_NAME)) {
          return ImmutableList.copyOf(
              javaBinary
                  .getDepsList()
                  .stream()
                  .filter(dep -> dep.startsWith("//third_party/maven/"))
                  .collect(Collectors.toList()));
        }
      }
    }
    return ImmutableList.of();
  }

  private List<String> getThirdPartyFolderNames(String repoName) throws IOException {
    return fileUtils
        .listContents(
            fileUtils.getCurrentWorkingDirectory() + String.format("/bazel-%s/external", repoName))
        .stream()
        .filter(folderName -> folderName.startsWith("mvn"))
        .collect(Collectors.toList());
  }

  private List<String> getJavaClassesFromJar(String absPath) throws IOException {
    return runCommand("jar tf " + absPath)
        .stream()
        .filter(item -> item.endsWith(".class"))
        .filter(item -> !item.contains("$"))
        .collect(Collectors.toList());
  }

  private String getFolderNameByTarget(List<String> thirdPartyFolderNames, String target) {
    for (String folderName : thirdPartyFolderNames) {
      String targetName = target.substring(target.lastIndexOf('/') + 1);
      if (!targetName.contains(":")) {
        target += ":" + targetName;
      }
      if (target
          .replace("//third_party/maven/", "mvn")
          .replace("/", "_")
          .replace(":", "_")
          .equals(folderName)) {
        return folderName;
      }
    }
    log.atWarning().log("Can't find the associated folder name for: %s", target);
    return "";
  }

  private ImmutableList<String> runCommand(String command) throws IOException {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    Process process = Runtime.getRuntime().exec(command);

    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = stdout.readLine()) != null) {
      result.add(line);
    }

    BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while ((line = stderr.readLine()) != null) {
      System.err.println(line);
    }
    return result.build();
  }
}

