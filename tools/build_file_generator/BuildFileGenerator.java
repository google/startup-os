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
import java.util.List;
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
  private ThirdPartyDeps thirdPartyDeps;

  @Inject
  public BuildFileGenerator(
      FileUtils fileUtils,
      ProtoFileAnalyzer protoFileAnalyzer,
      JavaClassAnalyzer javaClassAnalyzer) {
    this.fileUtils = fileUtils;
    this.protoFileAnalyzer = protoFileAnalyzer;
    this.javaClassAnalyzer = javaClassAnalyzer;
    thirdPartyDeps = getThirdPartyDeps();
  }

  private ThirdPartyDeps getThirdPartyDeps() {
    return (ThirdPartyDeps)
        fileUtils.readPrototxtFromZipUnchecked(
            THIRD_PARTY_ZIP_PATH, PROTOTXT_FILENAME_INSIDE_ZIP, ThirdPartyDeps.newBuilder());
  }

  BuildFile generateBuildFile(String packagePath) throws IOException {
    BuildFile.Builder result = BuildFile.newBuilder();

    List<String> protoFiles = getFilesByExtension(packagePath, ".proto");
    for (String protoFileName : protoFiles) {
      ProtoFile protoFile =
          protoFileAnalyzer.getProtoFile(fileUtils.joinPaths(packagePath, protoFileName));
      ProtoLibrary protoLibrary = getProtoLibrary(protoFile);
      result.addProtoLibrary(protoLibrary);
      result.addJavaProtoLibrary(getJavaProtoLibrary(protoLibrary.getName()));
      if (!protoFile.getServicesList().isEmpty()) {
        result.addJavaGrpcLibrary(getJavaGrpcLibrary(protoFile));

        LoadExtensionStatement javaGrpcLibrary =
            getLoadExtensionStatement(JAVA_GRPC_LIBRARY_BZL_FILE_PATH, JAVA_GRPC_LIBRARY_SYMBOL);
        if (!result.getExtensionList().contains(javaGrpcLibrary)) {
          result.addExtension(javaGrpcLibrary);
        }
      }
    }

    List<String> javaClasses = getFilesByExtension(packagePath, ".java");
    if (!javaClasses.isEmpty()) {
      result.addExtension(getLoadExtensionStatement(CHECKSTYLE_BZL_FILE_PATH, CHECKSTYLE_SYMBOL));
      for (String javaClassName : javaClasses) {
        JavaClass javaClass =
            javaClassAnalyzer.getJavaClass(fileUtils.joinPaths(packagePath, javaClassName));

        String targetName;
        if (javaClass.getHasMainMethod()) {
          JavaBinary javaBinary = getJavaBinary(javaClass);
          targetName = javaBinary.getName();
          result.addJavaBinary(javaBinary);
        } else if (javaClass.getIsTestClass()) {
          JavaTest javaTest = getJavaTest(javaClass, packagePath);
          targetName = javaTest.getName();
          result.addJavaTest(javaTest);
        } else {
          JavaLibrary javaLibrary = getJavaLibrary(javaClass);
          targetName = javaLibrary.getName();
          result.addJavaLibrary(getJavaLibrary(javaClass));
        }
        result.addCheckstyleTest(getCheckstyleTest(targetName));
      }
    }
    return result.build();
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
        .addAllDeps(protoFile.getImportsList())
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

  private JavaLibrary getJavaLibrary(JavaClass javaClass) {
    return JavaLibrary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(getDeps(javaClass))
        .build();
  }

  private JavaBinary getJavaBinary(JavaClass javaClass) {
    return JavaBinary.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .setMainClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addSrcs(javaClass.getClassName() + ".java")
        .addAllDeps(getDeps(javaClass))
        .build();
  }

  private JavaTest getJavaTest(JavaClass javaClass, String packagePath) {
    return JavaTest.newBuilder()
        .setName(convertUpperCamelToLowerUnderscore(javaClass.getClassName()))
        .addSrcs(javaClass.getClassName() + ".java")
        .setTestClass(javaClass.getPackage() + "." + javaClass.getClassName())
        .addAllDeps(getDeps(javaClass))
        .addAllResources(getResources(packagePath))
        .build();
  }

  private CheckstyleTestExtension getCheckstyleTest(String target) {
    return CheckstyleTestExtension.newBuilder()
        .setName(target + "-checkstyle")
        .setTarget(":" + target)
        .build();
  }

  // TODO: Implement method
  private List<String> getResources(String packagePath) {
    String resourcesPath = fileUtils.joinPaths(packagePath, "resources");
    System.out.println(resourcesPath);
    if (fileUtils.fileOrFolderExists(resourcesPath)) {
      System.out.println("1234567890");
      try {
        return fileUtils
            .listContents(resourcesPath)
            .stream()
            .map(resource -> "resources/" + resource)
            .collect(Collectors.toList());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return new ArrayList<>();
  }

  private List<String> getDeps(JavaClass javaClass) {
    List<String> result = new ArrayList<>();

    for (Import importProto : javaClass.getImportList()) {
      if (importProto.getPackage().startsWith("java")) {
        continue;
      }
      String classNameWithPackage =
          importProto.getPackage().replace(".", "/") + "/" + importProto.getClassName() + ".class";
      List<ThirdPartyDep> thirdPartyDepList = thirdPartyDeps.getThirdPartyDepList();
      List<ThirdPartyDep> thirdPartyTargets = new ArrayList<>();
      for (ThirdPartyDep thirdPartyDep : thirdPartyDepList) {
        if (thirdPartyDep.getJavaClassList().contains(classNameWithPackage)) {
          thirdPartyTargets.add(thirdPartyDep);
        }
      }

      String target;
      if (thirdPartyTargets.isEmpty()) {
        // It isn't third party dep.
        target = getInternalDep(importProto, getWholeProjectProtoFiles());
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
      if (!result.contains(target)) {
        result.add(target);
      }
    }
    return result.stream().sorted().collect(Collectors.toList());
  }

  private String getInternalDep(Import importProto, List<ProtoFile> protoFiles) {
    String path = importProto.getPackage().replace("com.google.startupos.", "//").replace(".", "/");
    for (ProtoFile protoFile : protoFiles) {
      if ((importProto.getPackage() + "." + importProto.getClassName())
          .startsWith(protoFile.getJavaPackage() + "." + protoFile.getJavaOuterClassname())) {
        return path + ":" + protoFile.getFileName() + "_java_proto";
      }
    }
    return path + ":" + convertUpperCamelToLowerUnderscore(importProto.getClassName());
  }

  // TODO: When generating multiple BUILD files, do this only once.
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

