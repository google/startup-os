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

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.build_file_generator.BuildFileParser;
import com.google.startupos.tools.build_file_generator.JavaClassAnalyzer;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDep;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDeps;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;
import com.google.startupos.tools.build_file_generator.Protos.WorkspaceFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class HttpArchiveDepsGenerator {
  private static final String STARTUP_OS_HTTP_ARCHIVE_NAME = "startup_os";
  private BuildFileParser buildFileParser;
  private JavaClassAnalyzer javaClassAnalyzer;
  private FileUtils fileUtils;
  private String projectName;

  @Inject
  public HttpArchiveDepsGenerator(
      BuildFileParser buildFileParser, JavaClassAnalyzer javaClassAnalyzer, FileUtils fileUtils) {
    this.buildFileParser = buildFileParser;
    this.javaClassAnalyzer = javaClassAnalyzer;
    this.fileUtils = fileUtils;
    projectName =
        fileUtils
            .getCurrentWorkingDirectory()
            .substring(fileUtils.getCurrentWorkingDirectory().lastIndexOf('/') + 1);
  }

  public HttpArchiveDeps getHttpArchiveDeps(WorkspaceFile workspaceFile, String absRepoPath)
      throws IOException {
    HttpArchiveDeps.Builder result = HttpArchiveDeps.newBuilder();

    for (WorkspaceFile.HttpArchive httpArchive : workspaceFile.getHttpArchiveList()) {
      if (httpArchive.getName().equals(STARTUP_OS_HTTP_ARCHIVE_NAME)) {
        System.out.println(fileUtils.folderExists(absRepoPath));
        ImmutableList<String> getBuildFilesAbsPaths = getBuildFilesAbsPaths(absRepoPath);
        for (String path : getBuildFilesAbsPaths) {
          BuildFile buildFile = buildFileParser.getBuildFile(path);
          for (BuildFile.JavaLibrary javaLibrary : buildFile.getJavaLibraryList()) {
            addDeps(absRepoPath, result, path, javaLibrary.getSrcsList(), javaLibrary.getName());
          }
          for (BuildFile.JavaBinary javaBinary : buildFile.getJavaBinaryList()) {
            addDeps(absRepoPath, result, path, javaBinary.getSrcsList(), javaBinary.getName());
          }
        }
        result.setCommitId(getCommitId(httpArchive.getUrls(0)));
      }
    }
    return result.build();
  }

  private ImmutableList<String> getBuildFilesAbsPaths(String absRepoPath) {
    try {
      return ImmutableList.copyOf(
          fileUtils
              .listContentsRecursively(absRepoPath)
              .stream()
              .filter(path -> path.endsWith("/BUILD"))
              .filter(path -> !path.contains("/third_party/maven/"))
              .collect(Collectors.toList()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addDeps(
      String absRepoPath,
      HttpArchiveDeps.Builder result,
      String absBuildFilePath,
      List<String> javaClasses,
      String targetName)
      throws IOException {
    for (String javaClassName : javaClasses) {
      String absJavaClassPath = absBuildFilePath.replace("BUILD", javaClassName);
      if (fileUtils.fileExists(absJavaClassPath)) {
        JavaClass javaClass = javaClassAnalyzer.getJavaClass(absJavaClassPath);
        result.addHttpArchiveDep(
            HttpArchiveDep.newBuilder()
                .setJavaClass(
                    getProjectPackageSuffix(javaClass.getPackage())
                        + absBuildFilePath
                            .replace("/BUILD", ".")
                            .replace(absRepoPath, "")
                            .replace("/", ".")
                        + javaClassName)
                .setTarget(
                    absBuildFilePath.replace(absRepoPath, "/").replace("/BUILD", ":") + targetName)
                .build());
      }
    }
  }

  private String getProjectPackageSuffix(String classPackage) {
    String[] classPackageParts = classPackage.split(projectName.replace("-", ""));
    String absFilesystemPackagePath =
        fileUtils.getCurrentWorkingDirectory()
            + classPackageParts[classPackageParts.length - 1].replace(".", "/");
    String[] absPathParts = absFilesystemPackagePath.split(projectName);
    String filesystemPackage =
        absPathParts[absPathParts.length - 1]
            .replaceFirst("/", "")
            .replaceAll("/$", "")
            .replace("/", ".");
    if (classPackage.contains(filesystemPackage)) {
      return classPackage.replace(filesystemPackage, "").replaceAll(".$", "");
    }
    return "";
  }

  private String getCommitId(String repoUrl) {
    return repoUrl.substring(repoUrl.lastIndexOf('/') + 1).replace(".zip", "");
  }
}

