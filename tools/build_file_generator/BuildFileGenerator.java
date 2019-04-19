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

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.CheckstyleTestExtension;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaBinary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaGrpcLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaProtoLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaTest;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.LoadExtensionStatement;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.ProtoLibrary;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDep;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDeps;
import com.google.startupos.tools.build_file_generator.Protos.HttpArchiveDepsList;
import com.google.startupos.tools.build_file_generator.Protos.Import;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;
import com.google.startupos.tools.build_file_generator.Protos.ProtoFile;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDep;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDeps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

// TODO: Add the support full classnames(package + classname) inside the code.
public class BuildFileGenerator {
  private static final String THIRD_PARTY_ZIP_PATH = "third_party_deps.zip";
  private static final String PROTOTXT_FILENAME_INSIDE_ZIP = "third_party_deps.prototxt";
  private static final String HTTP_ARCHIVE_DEPS_PATH = "http_archive_deps.prototxt";
  private static final String CHECKSTYLE_BZL_FILE_PATH = "//tools/checkstyle:checkstyle.bzl";
  private static final String CHECKSTYLE_SYMBOL = "checkstyle_test";
  private static final String JAVA_GRPC_LIBRARY_BZL_FILE_PATH =
      "//third_party:java_grpc_library.bzl";
  private static final String JAVA_GRPC_LIBRARY_SYMBOL = "java_grpc_library";
  private static final String CHECKSTYLE_TEST_CONFIG = "//tools/checkstyle:config.xml";
  private static final String STARTUP_OS_PROJECT_NAME = "startup-os";
  private static final ImmutableList<String> BUILD_FILE_GENERATION_BLACKLIST =
      ImmutableList.of(
          // A `resources` folder can contain proto files. If the `resources` folder contains a
          // BUILD file, it impossible to use proto files as a label in a `resources` argument.
          "resources",
          // It's a subfolder. BUILD file from them is in the parent folder.
          "/tools/reviewer/aa/commands/checks",
          // Currently, we don't support auto-generating BUIlD files for android projects
          "/examples/android/activities");

  private FileUtils fileUtils;
  private ProtoFileAnalyzer protoFileAnalyzer;
  private JavaClassAnalyzer javaClassAnalyzer;
  private BuildFileParser buildFileParser;
  private String projectName;

  @Inject
  public BuildFileGenerator(
      FileUtils fileUtils,
      ProtoFileAnalyzer protoFileAnalyzer,
      JavaClassAnalyzer javaClassAnalyzer,
      BuildFileParser buildFileParser) {
    this.fileUtils = fileUtils;
    this.protoFileAnalyzer = protoFileAnalyzer;
    this.javaClassAnalyzer = javaClassAnalyzer;
    this.buildFileParser = buildFileParser;
    projectName =
        fileUtils
            .getCurrentWorkingDirectory()
            .substring(fileUtils.getCurrentWorkingDirectory().lastIndexOf('/') + 1);
  }

  private ThirdPartyDeps getThirdPartyDeps() {
    return (ThirdPartyDeps)
        fileUtils.readPrototxtFromZipUnchecked(
            THIRD_PARTY_ZIP_PATH, PROTOTXT_FILENAME_INSIDE_ZIP, ThirdPartyDeps.newBuilder());
  }

  private HttpArchiveDepsList getHttpArchiveDepsList() {
    return (HttpArchiveDepsList)
        fileUtils.readPrototxtUnchecked(HTTP_ARCHIVE_DEPS_PATH, HttpArchiveDepsList.newBuilder());
  }

  // Returns absolute paths where exists java classes and/or proto files.
  // These are places where we should create BUILD files.
  private ImmutableList<String> getPathsToCreateBuildFiles() {
    Set<String> result = new HashSet<>();
    try {
      for (String path :
          fileUtils.listContentsRecursively(fileUtils.getCurrentWorkingDirectory())) {
        if (fileUtils.folderExists(path)) {
          List<String> folderContent = fileUtils.listContents(path);
          for (String fileName : folderContent) {
            if (fileName.endsWith(".java") || fileName.endsWith(".proto")) {
              boolean isPathToGenerationBuildFile = true;
              for (String item : BUILD_FILE_GENERATION_BLACKLIST) {
                if (path.endsWith(item)) {
                  isPathToGenerationBuildFile = false;
                }
              }
              if (isPathToGenerationBuildFile) {
                result.add(path);
              }
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ImmutableList.copyOf(result);
  }

  /* Returns Map<String, BuildFile> where
  String key is an absolute path to a folder where BUILD file should be created,
  BuildFile is the proto message. */
  Map<String, BuildFile> generateBuildFiles() throws IOException {
    Map<String, BuildFile> result = new HashMap<>();
    List<ProtoFile> wholeProjectProtoFiles = getWholeProjectProtoFiles();
    for (String packagePath : getPathsToCreateBuildFiles()) {
      result.put(packagePath, generateBuildFile(packagePath, wholeProjectProtoFiles));
    }
    return result;
  }

  private BuildFile generateBuildFile(String packagePath, List<ProtoFile> wholeProjectProtoFiles)
      throws IOException {
    BuildFile.Builder buildFile = BuildFile.newBuilder();

    List<String> protoFilenames = getFilesByExtension(packagePath, ".proto");
    List<ProtoFile> protoFiles = new ArrayList<>();
    for (String protoFileName : protoFilenames) {
      ProtoFile protoFile =
          protoFileAnalyzer.getProtoFile(fileUtils.joinPaths(packagePath, protoFileName));
      protoFiles.add(protoFile);
      ProtoLibrary protoLibrary = getProtoLibrary(protoFile);
      buildFile.addProtoLibrary(protoLibrary);
      buildFile.addJavaProtoLibrary(getJavaProtoLibrary(protoLibrary.getName()));
      if (!protoFile.getServicesList().isEmpty()) {
        buildFile.addJavaGrpcLibrary(getJavaGrpcLibrary(protoFile));

        LoadExtensionStatement javaGrpcLibrary =
            getLoadExtensionStatement(JAVA_GRPC_LIBRARY_BZL_FILE_PATH, JAVA_GRPC_LIBRARY_SYMBOL);
        if (!buildFile.getExtensionList().contains(javaGrpcLibrary)) {
          buildFile.addExtension(javaGrpcLibrary);
        }
      }
    }

    List<String> javaClasses = getFilesByExtension(packagePath, ".java");
    List<ThirdPartyDep> thirdPartyDeps = getThirdPartyDeps().getThirdPartyDepList();
    HttpArchiveDepsList httpArchiveDepsList = getHttpArchiveDepsList();
    if (!javaClasses.isEmpty()) {
      buildFile.addExtension(
          getLoadExtensionStatement(CHECKSTYLE_BZL_FILE_PATH, CHECKSTYLE_SYMBOL));

      for (String javaClassName : javaClasses) {
        JavaClass javaClass =
            javaClassAnalyzer.getJavaClass(fileUtils.joinPaths(packagePath, javaClassName));

        String targetName;
        if (javaClass.getHasMainMethod()) {
          JavaBinary javaBinary =
              getJavaBinary(
                  javaClass,
                  thirdPartyDeps,
                  httpArchiveDepsList,
                  protoFiles,
                  wholeProjectProtoFiles);
          targetName = javaBinary.getName();
          buildFile.addJavaBinary(javaBinary);
        } else if (javaClass.getIsTestClass()) {
          JavaTest javaTest =
              getJavaTest(
                  javaClass,
                  packagePath,
                  thirdPartyDeps,
                  httpArchiveDepsList,
                  protoFiles,
                  wholeProjectProtoFiles);
          targetName = javaTest.getName();
          buildFile.addJavaTest(javaTest);
        } else {
          JavaLibrary javaLibrary =
              getJavaLibrary(
                  javaClass,
                  thirdPartyDeps,
                  httpArchiveDepsList,
                  protoFiles,
                  wholeProjectProtoFiles);
          targetName = javaLibrary.getName();
          buildFile.addJavaLibrary(javaLibrary);
        }
        buildFile.addCheckstyleTest(getCheckstyleTest(targetName));
      }
    }
    return buildFile.build();
  }

  private List<String> getFilesByExtension(String packagePath, String fileExtension)
      throws IOException {
    return fileUtils
        .listContents(packagePath)
        .stream()
        .filter(file -> file.endsWith(fileExtension))
        .collect(Collectors.toList());
  }

  private ProtoLibrary getProtoLibrary(ProtoFile protoFile) {
    return ProtoLibrary.newBuilder()
        .setName(protoFile.getFileName() + "_proto")
        .addSrcs(protoFile.getFileName())
        .addAllDeps(
            protoFile
                .getImportsList()
                .stream()
                .map(this::getProtoLibraryNameFromProtoImportStatement)
                .collect(Collectors.toList()))
        .build();
  }

  // Converts import proto statement to proto library name. E.g. `tools/reviewer/reviewer.proto`
  // converts to `//tools/reviewer:reviewer_proto`
  private String getProtoLibraryNameFromProtoImportStatement(String protoImport) {
    return "//"
        + protoImport.substring(0, protoImport.lastIndexOf('/'))
        + ":"
        + protoImport.substring(protoImport.lastIndexOf('/') + 1).replace(".", "_");
  }

  private JavaProtoLibrary getJavaProtoLibrary(String protoLibraryName) {
    return JavaProtoLibrary.newBuilder()
        .setName(protoLibraryName.replace("_proto", "_java_proto"))
        .addDeps(":" + protoLibraryName)
        .build();
  }

  private JavaGrpcLibrary getJavaGrpcLibrary(ProtoFile protoFile) {
    return JavaGrpcLibrary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(protoFile.getFileName()) + "_java_grpc")
        .addSrcs(":" + protoFile.getFileName() + "_proto")
        .addTags("checkstyle_ignore")
        .addDeps(":" + protoFile.getFileName() + "_java_proto")
        .build();
  }

  private LoadExtensionStatement getLoadExtensionStatement(String path, String symbol) {
    return LoadExtensionStatement.newBuilder().setPath(path).setSymbol(symbol).build();
  }

  private JavaLibrary getJavaLibrary(
      JavaClass javaClass,
      List<ThirdPartyDep> thirdPartyDeps,
      HttpArchiveDepsList httpArchiveDepsList,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaLibrary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(
            getDeps(
                javaClass, thirdPartyDeps, httpArchiveDepsList, protoFiles, wholeProjectProtoFiles))
        .build();
  }

  private JavaBinary getJavaBinary(
      JavaClass javaClass,
      List<ThirdPartyDep> thirdPartyDeps,
      HttpArchiveDepsList httpArchiveDepsList,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaBinary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .setMainClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(
            getDeps(
                javaClass, thirdPartyDeps, httpArchiveDepsList, protoFiles, wholeProjectProtoFiles))
        // TODO: Add it to each java_binary only when we really need it.
        .addDeps("//third_party/maven/com/google/flogger:flogger_system_backend")
        .build();
  }

  private JavaTest getJavaTest(
      JavaClass javaClass,
      String packagePath,
      List<ThirdPartyDep> thirdPartyDeps,
      HttpArchiveDepsList httpArchiveDepsList,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaTest.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .setTestClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addAllDeps(
            getDeps(
                javaClass, thirdPartyDeps, httpArchiveDepsList, protoFiles, wholeProjectProtoFiles))
        // TODO: Add it to each java_binary only when we really need it.
        .addDeps("//third_party/maven/com/google/flogger:flogger_system_backend")
        .addAllResources(getResources(packagePath))
        .build();
  }

  private CheckstyleTestExtension getCheckstyleTest(String target) {
    CheckstyleTestExtension.Builder result = CheckstyleTestExtension.newBuilder();
    result.setName(target + "-checkstyle").setTarget(":" + target);
    if (projectName.equals(STARTUP_OS_PROJECT_NAME)) {
      return result.build();
    } else {
      return result.setConfig(CHECKSTYLE_TEST_CONFIG).build();
    }
  }

  private List<String> getResources(String packagePath) {
    String resourcesPath = fileUtils.joinPaths(packagePath, "resources");
    if (fileUtils.fileOrFolderExists(resourcesPath)) {
      try {
        return fileUtils
            .listContents(resourcesPath)
            .stream()
            .map(resource -> "resources/" + resource)
            .collect(Collectors.toList());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new ArrayList<>();
  }

  // TODO: Add support java_grpc libraries
  private List<String> getDeps(
      JavaClass javaClass,
      List<ThirdPartyDep> thirdPartyDeps,
      HttpArchiveDepsList httpArchiveDepsList,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    Set<String> result = new HashSet<>();
    Map<String, String> internalProjectDeps = getInternalProjectJavaClassToTargetNameMap();
    List<String> internalPackageDeps = getInternalPackageDeps(javaClass, protoFiles);
    result.addAll(internalPackageDeps);
    for (Import importProto : javaClass.getImportList()) {
      if (importProto.getPackage().startsWith("java.")
          || importProto.getPackage().startsWith("com.sun.net.")) {
        continue;
      }
      List<ThirdPartyDep> thirdPartyTargets = new ArrayList<>();
      for (ThirdPartyDep thirdPartyDep : thirdPartyDeps) {
        if (thirdPartyDep
            .getJavaClassList()
            .contains(
                importProto.getPackage().replace(".", "/")
                    + "/"
                    + importProto.getRootClass()
                    + ".class")) {
          thirdPartyTargets.add(thirdPartyDep);
        }
      }

      String httpArchiveTarget = "";
      if (httpArchiveTarget.isEmpty()) {
        for (HttpArchiveDeps httpArchiveDeps : httpArchiveDepsList.getHttpArchiveDepsList()) {
          for (HttpArchiveDep httpArchiveDep : httpArchiveDeps.getHttpArchiveDepList()) {
            if (httpArchiveDep
                .getJavaClass()
                .contains(importProto.getPackage() + "." + importProto.getRootClass())) {
              httpArchiveTarget = '@' + httpArchiveDeps.getName() + httpArchiveDep.getTarget();
              break;
            }
          }
          if (!httpArchiveTarget.isEmpty()) {
            break;
          }
        }
      }

      String target;
      if (thirdPartyTargets.isEmpty()) {
        // It isn't third party dep.
        if (httpArchiveTarget.isEmpty()) {
          String classNameWithPackage = importProto.getPackage() + "." + importProto.getRootClass();
          if (internalProjectDeps.containsKey(classNameWithPackage)) {
            target = internalProjectDeps.get(classNameWithPackage);
          } else {
            target =
                getInternalProjectDep(importProto, wholeProjectProtoFiles, javaClass.getPackage());
          }
        } else {
          target = httpArchiveTarget;
        }
      } else if (thirdPartyTargets.size() == 1) {
        target = thirdPartyTargets.get(0).getTarget();
      } else {
        // If the class exists in several third party deps we choose the smallest one
        // since the other ones probably contain it
        target =
            thirdPartyTargets
                .stream()
                .sorted(Comparator.comparing(ThirdPartyDep::getJavaClassCount))
                .collect(Collectors.toList())
                .get(0)
                .getTarget();
      }
      if (target.endsWith("//third_party/maven/com/google/dagger:dagger")
          || target.endsWith("//third_party/maven/com/google/dagger")
          || target.endsWith("//common:dagger_common_component")) {
        if (projectName.equals(STARTUP_OS_PROJECT_NAME)) {
          target = "//common:dagger_with_annotation_processor";
        } else {
          target = "@startup_os//common:dagger_with_annotation_processor";
        }
      }
      // TODO: After //common/repo/BUILD file will be generated automatically change this. If
      // `//common/repo:git_repo` exists then we could generalize to remove `_factory`.
      if (target.equals("//common/repo:git_repo_factory")) {
        if (projectName.equals(STARTUP_OS_PROJECT_NAME)) {
          target = "//common/repo:repo";
        } else {
          target = "@startup_os//common/repo:repo";
        }
      }
      if (!target.isEmpty()) {
        result.add(target);
      }
    }
    return result.stream().sorted().collect(Collectors.toList());
  }

  private String getInternalProjectDep(
      Import importProto, List<ProtoFile> protoFiles, String packageToCreateBuildFile) {
    final String projectPackageSuffix = getProjectPackageSuffix(importProto.getPackage());
    String path =
        importProto.getPackage().replace(projectPackageSuffix + ".", "//").replace(".", "/");
    for (ProtoFile protoFile : protoFiles) {
      for (String service : protoFile.getServicesList()) {
        if ((service + "Grpc").equals(importProto.getRootClass())) {
          String javaGrpcName = ":" + protoFile.getFileName() + "_java_grpc";
          if (importProto.getPackage().equals(packageToCreateBuildFile)) {
            return javaGrpcName;
          } else {
            return protoFile
                    .getPackage()
                    .replace(projectPackageSuffix + ".", "//")
                    .replace(".", "/")
                + javaGrpcName;
          }
        }
      }

      String javaProtoName = ":" + protoFile.getFileName() + "_java_proto";
      if ((importProto.getPackage().equals(protoFile.getJavaPackage())
          && importProto.getRootClass().equals(protoFile.getJavaOuterClassname()))) {
        if (importProto.getPackage().equals(packageToCreateBuildFile)) {
          return javaProtoName;
        } else {
          return protoFile.getPackage().replace(projectPackageSuffix + ".", "//").replace(".", "/")
              + javaProtoName;
        }
      }
    }
    if (importProto.getRootClass().isEmpty()) {
      return path + ":" + convertUpperCamelToLowerUnderscore(importProto.getClassName());
    } else {
      return path + ":" + convertUpperCamelToLowerUnderscore(importProto.getRootClass());
    }
  }

  private List<String> getInternalPackageDeps(JavaClass javaClass, List<ProtoFile> protoFiles) {
    List<String> result = new ArrayList<>();
    Map<String, List<String>> protoFilenameToMessage = new HashMap<>();
    Map<String, List<String>> protoFilenameToService = new HashMap<>();
    for (ProtoFile protoFile : protoFiles) {
      List<String> protoMessages = new ArrayList<>();
      protoMessages.addAll(protoFile.getMessagesList());
      protoMessages.add(protoFile.getJavaOuterClassname());
      protoFilenameToMessage.put(protoFile.getFileName(), protoMessages);
      protoFilenameToService.put(protoFile.getFileName(), protoFile.getServicesList());
    }
    for (String classname : javaClass.getUsedClassesFromTheSamePackageList()) {
      String dep = "";
      for (Map.Entry<String, List<String>> entry : protoFilenameToService.entrySet()) {
        for (String service : entry.getValue()) {
          if ((service + "Grpc").equals(classname)) {
            dep = ":" + entry.getKey() + "_java_grpc";
          }
        }
      }
      for (Map.Entry<String, List<String>> entry : protoFilenameToMessage.entrySet()) {
        if (entry.getValue().contains(classname)) {
          dep = ":" + entry.getKey() + "_java_proto";
          break;
        }
      }
      if (dep.isEmpty()) {
        dep = ":" + convertUpperCamelToLowerUnderscore(classname);
      }
      result.add(dep);
    }
    return result.stream().distinct().collect(Collectors.toList());
  }

  // TODO: Add supporting glob function
  private Map<String, String> getInternalProjectJavaClassToTargetNameMap() {
    Map<String, String> result = new HashMap<>();

    for (String absPath : getPathsToCreateBuildFiles()) {
      final String relPackagePath = absPath.replace(fileUtils.getCurrentWorkingDirectory(), "/");
      final String absBuildFilePath = fileUtils.joinPaths(absPath, "BUILD");

      if (fileUtils.fileExists(absBuildFilePath)) {
        BuildFile buildFile = buildFileParser.getBuildFile(absBuildFilePath);
        for (JavaLibrary javaLibrary : buildFile.getJavaLibraryList()) {
          if (javaLibrary.getSrcsCount() > 0) {
            String absClassPath = "/" + fileUtils.joinPaths(absPath, javaLibrary.getSrcs(0));
            try {
              JavaClass javaClass = javaClassAnalyzer.getJavaClass(absClassPath);
              final String projectPackageSuffix = getProjectPackageSuffix(javaClass.getPackage());
              String targetName = relPackagePath + ":" + javaLibrary.getName();
              for (String src : javaLibrary.getSrcsList()) {
                String fullClassName =
                    relPackagePath.replace("//", "").replace("/", ".") + "." + src;
                if (!projectPackageSuffix.isEmpty()) {
                  fullClassName = projectPackageSuffix + "." + fullClassName;
                }
                result.put(fullClassName.replace(".java", ""), targetName);
              }
            } catch (IOException e) {
              throw new RuntimeException("Can't find java class by path: " + absClassPath, e);
            }
          }
        }
      }
    }
    return result;
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

  private ImmutableList<ProtoFile> getWholeProjectProtoFiles() {
    ImmutableList.Builder<ProtoFile> result = ImmutableList.builder();
    try {
      List<String> protoPaths =
          fileUtils
              .listContentsRecursively(fileUtils.getCurrentWorkingDirectory())
              .stream()
              .filter(p -> p.endsWith(".proto"))
              .collect(Collectors.toList());
      for (String protoPath : protoPaths) {
        result.add(protoFileAnalyzer.getProtoFile(protoPath));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.build();
  }

  private String convertUpperCamelToLowerUnderscore(String string) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
  }
}

