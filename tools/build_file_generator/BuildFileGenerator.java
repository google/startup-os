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
import com.google.startupos.tools.build_file_generator.Protos.Import;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;
import com.google.startupos.tools.build_file_generator.Protos.ProtoFile;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDep;
import com.google.startupos.tools.build_file_generator.Protos.ThirdPartyDeps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class BuildFileGenerator {
  private static final String THIRD_PARTY_ZIP_PATH =
      "tools/build_file_generator/third_party_deps.zip";
  private static final String PROTOTXT_FILENAME_INSIDE_ZIP = "third_party_deps.prototxt";
  private static final String CHECKSTYLE_BZL_FILE_PATH = "//tools/checkstyle:checkstyle.bzl";
  private static final String CHECKSTYLE_SYMBOL = "checkstyle_test";
  private static final String JAVA_GRPC_LIBRARY_BZL_FILE_PATH =
      "//third_party:java_grpc_library.bzl";
  private static final String JAVA_GRPC_LIBRARY_SYMBOL = "java_grpc_library";

  private FileUtils fileUtils;
  private ProtoFileAnalyzer protoFileAnalyzer;
  private JavaClassAnalyzer javaClassAnalyzer;
  private BuildFileParser buildFileParser;

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
  }

  private ThirdPartyDeps getThirdPartyDeps() {
    return (ThirdPartyDeps)
        fileUtils.readPrototxtFromZipUnchecked(
            THIRD_PARTY_ZIP_PATH, PROTOTXT_FILENAME_INSIDE_ZIP, ThirdPartyDeps.newBuilder());
  }

  // Returns absolute paths where exists java classes and/or proto files.
  // These are places where we should create BUILD files.
  private List<String> getPathsToCreateBuildFiles() {
    List<String> result = new ArrayList<>();
    try {
      for (String item :
          fileUtils.listContentsRecursively(fileUtils.getCurrentWorkingDirectory())) {
        if (fileUtils.folderExists(item)) {
          List<String> folderContent = fileUtils.listContents(item);
          for (String fileName : folderContent) {
            if (fileName.endsWith(".java") || fileName.endsWith(".proto")) {
              result.add(item);
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
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
    if (!javaClasses.isEmpty()) {
      buildFile.addExtension(
          getLoadExtensionStatement(CHECKSTYLE_BZL_FILE_PATH, CHECKSTYLE_SYMBOL));

      for (String javaClassName : javaClasses) {
        JavaClass javaClass =
            javaClassAnalyzer.getJavaClass(fileUtils.joinPaths(packagePath, javaClassName));

        String targetName;
        if (javaClass.getHasMainMethod()) {
          JavaBinary javaBinary =
              getJavaBinary(javaClass, thirdPartyDeps, protoFiles, wholeProjectProtoFiles);
          targetName = javaBinary.getName();
          buildFile.addJavaBinary(javaBinary);
        } else if (javaClass.getIsTestClass()) {
          JavaTest javaTest =
              getJavaTest(
                  javaClass, packagePath, thirdPartyDeps, protoFiles, wholeProjectProtoFiles);
          targetName = javaTest.getName();
          buildFile.addJavaTest(javaTest);
        } else {
          JavaLibrary javaLibrary =
              getJavaLibrary(javaClass, thirdPartyDeps, protoFiles, wholeProjectProtoFiles);
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
                .map(protoImport -> "//" + protoImport)
                .collect(Collectors.toList()))
        .build();
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
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaLibrary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(getDeps(javaClass, thirdPartyDeps, protoFiles, wholeProjectProtoFiles))
        .build();
  }

  private JavaBinary getJavaBinary(
      JavaClass javaClass,
      List<ThirdPartyDep> thirdPartyDeps,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaBinary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .setMainClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(getDeps(javaClass, thirdPartyDeps, protoFiles, wholeProjectProtoFiles))
        // TODO: Add it to each java_binary only when we really need it.
        .addDeps("//third_party/maven/com/google/flogger:flogger_system_backend")
        .build();
  }

  private JavaTest getJavaTest(
      JavaClass javaClass,
      String packagePath,
      List<ThirdPartyDep> thirdPartyDeps,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    return JavaTest.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .setTestClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addAllDeps(getDeps(javaClass, thirdPartyDeps, protoFiles, wholeProjectProtoFiles))
        .addAllResources(getResources(packagePath))
        .build();
  }

  private CheckstyleTestExtension getCheckstyleTest(String target) {
    return CheckstyleTestExtension.newBuilder()
        .setName(target + "-checkstyle")
        .setTarget(":" + target)
        .build();
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

  private List<String> getDeps(
      JavaClass javaClass,
      List<ThirdPartyDep> thirdPartyDeps,
      List<ProtoFile> protoFiles,
      List<ProtoFile> wholeProjectProtoFiles) {
    List<String> result = new ArrayList<>();

    Map<String, String> internalProjectDeps = getStartuposJavaClassToTargetNameMap();
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
                    + importProto.getClassName()
                    + ".class")) {
          thirdPartyTargets.add(thirdPartyDep);
        }
      }

      String target;
      if (thirdPartyTargets.isEmpty()) {
        // It isn't third party dep.
        String classNameWithPackage = importProto.getPackage() + "." + importProto.getClassName();
        if (internalProjectDeps.containsKey(classNameWithPackage)) {
          target = internalProjectDeps.get(classNameWithPackage);
        } else {
          target = getInternalProjectDep(importProto, wholeProjectProtoFiles, internalPackageDeps);
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
      if (target.equals("//third_party/maven/com/google/dagger:dagger")) {
        target = "//common:dagger_with_annotation_processor";
      }
      if (!result.contains(target) && !target.isEmpty()) {
        result.add(target);
      }
    }
    return result.stream().sorted().collect(Collectors.toList());
  }

  private String getInternalProjectDep(
      Import importProto, List<ProtoFile> protoFiles, List<String> internalPackageDeps) {
    String path = importProto.getPackage().replace("com.google.startupos.", "//").replace(".", "/");
    for (ProtoFile protoFile : protoFiles) {
      String javaProtoName = ":" + protoFile.getFileName() + "_java_proto";
      if (internalPackageDeps.contains(javaProtoName)) {
        return "";
      }
      if ((importProto.getPackage() + "." + importProto.getClassName())
          .startsWith(protoFile.getJavaPackage() + "." + protoFile.getJavaOuterClassname())) {
        return javaProtoName;
      }
    }
    return path + ":" + convertUpperCamelToLowerUnderscore(importProto.getClassName());
  }

  private List<String> getInternalPackageDeps(JavaClass javaClass, List<ProtoFile> protoFiles) {
    List<String> result = new ArrayList<>();
    Map<String, List<String>> protoFilenameToMessage = new HashMap<>();
    for (ProtoFile protoFile : protoFiles) {
      protoFilenameToMessage.put(protoFile.getFileName(), protoFile.getMessagesList());
    }
    for (String classname : javaClass.getUsedClassesFromTheSamePackageList()) {
      String dep = "";
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

  private Map<String, String> getStartuposJavaClassToTargetNameMap() {
    Map<String, String> result = new HashMap<>();

    for (String absPath : getPathsToCreateBuildFiles()) {
      final String relPackagePath = absPath.replace(fileUtils.getCurrentWorkingDirectory(), "/");
      final String absBuildFilePath = fileUtils.joinPaths(absPath, "BUILD");

      if (fileUtils.fileExists(absBuildFilePath)) {
        BuildFile buildFile = buildFileParser.getBuildFile(absBuildFilePath);
        final String startuposImportSuffix = "com.google.startupos";
        for (JavaLibrary javaLibrary : buildFile.getJavaLibraryList()) {
          String targetName = relPackagePath + ":" + javaLibrary.getName();
          for (String src : javaLibrary.getSrcsList()) {
            String fullClassName =
                startuposImportSuffix
                    + relPackagePath.replace("//", ".").replace("/", ".")
                    + "."
                    + src;
            result.put(fullClassName.replace(".java", ""), targetName);
          }
        }
      }
    }
    return result;
  }

  private List<ProtoFile> getWholeProjectProtoFiles() {
    List<ProtoFile> result = new ArrayList<>();
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
    return result;
  }

  private String convertUpperCamelToLowerUnderscore(String string) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
  }
}

