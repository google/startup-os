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
import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.build_file_generator.BuildFileParser;
import com.google.startupos.tools.build_file_generator.JavaClassAnalyzer;
import com.google.startupos.tools.build_file_generator.ProtoFileAnalyzer;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDep;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDeps;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDepsList;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;
import com.google.startupos.tools.build_file_generator.Protos.ProtoFile;
import com.google.startupos.tools.build_file_generator.Protos.WorkspaceFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class HttpArchiveDepsGenerator {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String BUILD_GENERATOR_TEMP_FOLDER = "build_generator_tmp";

  public static final String HTTP_ARCHIVE_DEPS_FILENAME = "http_archive_deps.prototxt";

  private BuildFileParser buildFileParser;
  private JavaClassAnalyzer javaClassAnalyzer;
  private ProtoFileAnalyzer protoFileAnalyzer;
  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;

  @Inject
  public HttpArchiveDepsGenerator(
      BuildFileParser buildFileParser,
      JavaClassAnalyzer javaClassAnalyzer,
      ProtoFileAnalyzer protoFileAnalyzer,
      FileUtils fileUtils,
      GitRepoFactory gitRepoFactory) {
    this.buildFileParser = buildFileParser;
    this.javaClassAnalyzer = javaClassAnalyzer;
    this.protoFileAnalyzer = protoFileAnalyzer;
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
  }

  public HttpArchiveDepsList getHttpArchiveDeps(
      WorkspaceFile workspaceFile, List<String> httpArchiveNames) throws IOException {
    HttpArchiveDepsList.Builder result = HttpArchiveDepsList.newBuilder();
    for (String httpArchiveName : httpArchiveNames) {
      WorkspaceFile.HttpArchive httpArchive = WorkspaceFile.HttpArchive.getDefaultInstance();
      for (WorkspaceFile.HttpArchive currentHttpArchive : workspaceFile.getHttpArchiveList()) {
        if (currentHttpArchive.getName().equals(httpArchiveName)) {
          httpArchive = currentHttpArchive;
        }
      }
      if (httpArchive.getName().isEmpty()) {
        log.atWarning().log("Can't find %s http_archive in WORKSPACE file,", httpArchiveName);
        continue;
      }
      if (areCommitIdsTheSame(httpArchiveName, getCommitId(httpArchive.getStripPrefix()))) {
        log.atInfo().log(
            "Commit id in WORKSPACE file and commit id in \'%s\' file for \'%s\' http_archive "
                + "are the same. Nothing to update.",
            HTTP_ARCHIVE_DEPS_FILENAME, httpArchiveName);
        continue;
      }
      if (httpArchive.getName().equals(httpArchiveName)) {
        HttpArchiveDeps.Builder builder = HttpArchiveDeps.newBuilder();
        String url = httpArchive.getUrls(0).split("/archive")[0] + ".git";
        String repoName = url.substring(url.lastIndexOf('/') + 1).replace(".git", "");

        GitRepo gitRepo = createRepo(url, repoName);
        switchToCommit(gitRepo, httpArchive.getStripPrefix());

        String absRepoPath =
            fileUtils.joinPaths(
                fileUtils.getCurrentWorkingDirectory(), BUILD_GENERATOR_TEMP_FOLDER, repoName);
        ImmutableList<String> buildFilesAbsPaths = getBuildFilesAbsPaths(absRepoPath);
        for (String path : buildFilesAbsPaths) {
          BuildFile buildFile = buildFileParser.getBuildFile(path);
          for (BuildFile.JavaLibrary javaLibrary : buildFile.getJavaLibraryList()) {
            addDeps(absRepoPath, builder, path, javaLibrary.getSrcsList(), javaLibrary.getName());
          }
          for (BuildFile.JavaBinary javaBinary : buildFile.getJavaBinaryList()) {
            addDeps(absRepoPath, builder, path, javaBinary.getSrcsList(), javaBinary.getName());
          }
          for (BuildFile.ProtoLibrary protoLibrary : buildFile.getProtoLibraryList()) {
            String absProtoFilePath = path.replace("BUILD", protoLibrary.getSrcs(0));
            for (BuildFile.JavaProtoLibrary javaProtoLibrary :
                buildFile.getJavaProtoLibraryList()) {
              if (javaProtoLibrary.getDepsList().contains(":" + protoLibrary.getName())) {
                ProtoFile protoFile = protoFileAnalyzer.getProtoFile(absProtoFilePath);
                if ((!protoFile.getJavaPackage().isEmpty())
                    && (!protoFile.getJavaOuterClassname().isEmpty())) {
                  String fullJavaClassName =
                      protoFile.getJavaPackage() + "." + protoFile.getJavaOuterClassname();
                  String target =
                      path.replace(absRepoPath, "/").replace("/BUILD", ":")
                          + javaProtoLibrary.getName();
                  builder.addHttpArchiveDep(
                      HttpArchiveDep.newBuilder()
                          .setTarget(target)
                          .setJavaClass(fullJavaClassName)
                          .build());
                }
              }
            }
          }
        }
        builder
            .setName(getCommitId(httpArchive.getName()))
            .setCommitId(getCommitId(httpArchive.getStripPrefix()));
        result.addHttpArchiveDeps(builder.build());
        fileUtils.clearDirectoryUnchecked(
            fileUtils.joinPaths(
                fileUtils.getCurrentWorkingDirectory(), BUILD_GENERATOR_TEMP_FOLDER));
      } else {
        log.atWarning().log("Can't find http_archive with name: %s", httpArchiveName);
      }
    }
    fileUtils.deleteFileOrDirectoryIfExists(
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), BUILD_GENERATOR_TEMP_FOLDER));
    return result.build();
  }

  private GitRepo createRepo(String url, String repoName) {
    String absRepoPath =
        fileUtils.joinPaths(
            fileUtils.getCurrentWorkingDirectory(), BUILD_GENERATOR_TEMP_FOLDER, repoName);
    GitRepo gitRepo = gitRepoFactory.create(absRepoPath);
    gitRepo.cloneRepo(url, absRepoPath);
    return gitRepo;
  }

  private void switchToCommit(GitRepo gitRepo, String stripPrefix) {
    gitRepo.resetHard(getCommitId(stripPrefix));
  }

  private String getCommitId(String stripPrefix) {
    return stripPrefix.substring(stripPrefix.lastIndexOf('-') + 1);
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
        String fullTargetName =
            absBuildFilePath.replace(absRepoPath, "/").replace("/BUILD", ":") + targetName;
        result.addHttpArchiveDep(
            HttpArchiveDep.newBuilder()
                .setJavaClass(
                    getProjectPackageSuffix(javaClass.getPackage(), absRepoPath)
                        + absBuildFilePath
                            .replace("/BUILD", ".")
                            .replace(absRepoPath, "")
                            .replace("/", ".")
                        + javaClassName.replace("/", "."))
                .setTarget(shortenTarget(fullTargetName))
                .build());
      }
    }
  }

  private String getProjectPackageSuffix(String classPackage, String absRepoPath) {
    String projectName = absRepoPath.substring(absRepoPath.lastIndexOf('/') + 1);
    String[] classPackageParts = classPackage.split(projectName.replace("-", ""));
    String absFilesystemPackagePath =
        absRepoPath + classPackageParts[classPackageParts.length - 1].replace(".", "/");
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

  private boolean areCommitIdsTheSame(String httpArchiveName, String workspaceCommitId) {
    String absPrototxtPath =
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), HTTP_ARCHIVE_DEPS_FILENAME);
    if (!fileUtils.fileExists(absPrototxtPath)) {
      return false;
    }
    HttpArchiveDepsList httpArchiveDepsList =
        (HttpArchiveDepsList)
            fileUtils.readPrototxtUnchecked(absPrototxtPath, HttpArchiveDepsList.newBuilder());
    String prototxtCommitId = "";
    for (HttpArchiveDeps httpArchiveDeps : httpArchiveDepsList.getHttpArchiveDepsList()) {
      if (httpArchiveDeps.getName().equals(httpArchiveName)) {
        prototxtCommitId = httpArchiveDeps.getCommitId();
        break;
      }
    }
    if (prototxtCommitId.isEmpty()) {
      return false;
    } else {
      return workspaceCommitId.equals(prototxtCommitId);
    }
  }

  private String shortenTarget(String fullTargetName) {
    String targetFolderAndName = fullTargetName.substring(fullTargetName.lastIndexOf('/') + 1);
    String targetFolder = targetFolderAndName.split(":")[0];
    String targetName = targetFolderAndName.split(":")[1];
    if (targetFolder.equals(targetName)) {
      return fullTargetName.replace((":" + targetName), "");
    } else {
      return fullTargetName;
    }
  }
}

