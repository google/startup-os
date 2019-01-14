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

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.CheckstyleTestExtension;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.JavaBinary;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.JavaGrpcLibrary;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.JavaLibrary;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.JavaProtoLibrary;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.JavaTest;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.LoadExtensionStatement;
import com.google.startupos.tools.buildfilegenerator.Protos.BuildFile.ProtoLibrary;

import java.util.List;
import javax.inject.Inject;

/* This class saves a BuildFile proto to a BUILD file. If a BUILD file exists
and is not auto-generated, it writes to BUILD.generated. */
public class BuildFileWriter {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final String HEADER = "# THIS FILE IS AUTO-GENERATED";
  private static final String REGULAR_FILENAME = "BUILD";
  private static final String GENERATED_FILENAME = "BUILD.generated";
  private static final String PACKAGE_DEFINITION =
      "package(default_visibility = [\"//visibility:public\"])";
  private final FileUtils fileUtils;

  @Inject
  public BuildFileWriter(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public void write(BuildFile buildFile, String packagePath) {
    StringBuilder result = new StringBuilder();
    List<CheckstyleTestExtension> checkstyleTests = buildFile.getCheckstyleTestList();
    result
        .append(HEADER)
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append(PACKAGE_DEFINITION)
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append(getLoadExtensions(buildFile.getExtensionList()))
        .append(getProtoLibraries(buildFile.getProtoLibraryList()))
        .append(getJavaProtoLibraries(buildFile.getJavaProtoLibraryList()))
        .append(getJavaGrpcLibraries(buildFile.getJavaGrpcLibraryList()))
        .append(getJavaLibraries(buildFile.getJavaLibraryList(), checkstyleTests))
        .append(getJavaBinaries(buildFile.getJavaBinaryList(), checkstyleTests))
        .append(getJavaTests(buildFile.getJavaTestList(), checkstyleTests));

    String buildFilePath = fileUtils.joinPaths(packagePath, "BUILD");
    if (!fileUtils.fileExists(buildFilePath)
        || fileUtils.readFileUnchecked(buildFilePath).startsWith(HEADER)) {
      fileUtils.writeStringUnchecked(result.toString(), packagePath + "/" + REGULAR_FILENAME);
    } else {
      fileUtils.writeStringUnchecked(result.toString(), packagePath + "/" + GENERATED_FILENAME);
    }
  }

  private String getLoadExtensions(List<LoadExtensionStatement> loadExtensions) {
    StringBuilder result = new StringBuilder();
    for (LoadExtensionStatement loadExtension : loadExtensions) {
      result
          .append(
              String.format(
                  "load(\"%s\", \"%s\")", loadExtension.getPath(), loadExtension.getSymbol()))
          .append(System.lineSeparator());
    }
    result.append(System.lineSeparator());
    return result.toString();
  }

  private String getProtoLibraries(List<ProtoLibrary> protoLibraries) {
    StringBuilder result = new StringBuilder();

    for (ProtoLibrary protoLibrary : protoLibraries) {
      result
          .append("proto_library(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", protoLibrary.getName()))
          .append(System.lineSeparator());

      List<String> srcs = protoLibrary.getSrcsList();
      if (!srcs.isEmpty()) {
        result.append("  srcs = [");
        if (srcs.size() == 1) {
          result.append(String.format("\"%s\"],", srcs.get(0) + ".proto"));
        } else {
          result.append(System.lineSeparator());
          for (String src : srcs) {
            result.append(String.format("    \"%s\",", src + ".proto"));
            result.append(System.lineSeparator());
          }
          result.append("  ],");
        }
      }
      result
          .append(getArgument("deps", protoLibrary.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
    }
    return result.toString();
  }

  private String getJavaProtoLibraries(List<JavaProtoLibrary> javaProtoLibraries) {
    StringBuilder result = new StringBuilder();
    for (JavaProtoLibrary javaProtoLibrary : javaProtoLibraries) {
      result
          .append("java_proto_library(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", javaProtoLibrary.getName()))
          .append(getArgument("deps", javaProtoLibrary.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
    }
    return result.toString();
  }

  private String getJavaGrpcLibraries(List<JavaGrpcLibrary> javaGrpcLibraries) {
    StringBuilder result = new StringBuilder();
    for (JavaGrpcLibrary grpcLibrary : javaGrpcLibraries) {
      result
          .append("java_grpc_library(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", grpcLibrary.getName()))
          .append(getArgument("srcs", grpcLibrary.getSrcsList()))
          .append(getArgument("tags", grpcLibrary.getTagsList()))
          .append(getArgument("deps", grpcLibrary.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
    }
    return result.toString();
  }

  private String getJavaLibraries(
      List<JavaLibrary> javaLibraries, List<CheckstyleTestExtension> checkstyleTests) {
    StringBuilder result = new StringBuilder();
    for (JavaLibrary javaLibrary : javaLibraries) {
      result
          .append("java_library(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", javaLibrary.getName()))
          .append(getArgument("srcs", javaLibrary.getSrcsList()))
          .append(getArgument("deps", javaLibrary.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
      result.append(getCheckstyleTest(checkstyleTests, javaLibrary.getName()));
    }
    return result.toString();
  }

  private String getJavaBinaries(
      List<JavaBinary> javaBinaries, List<CheckstyleTestExtension> checkstyleTests) {
    StringBuilder result = new StringBuilder();
    for (JavaBinary javaBinary : javaBinaries) {
      result
          .append("java_binary(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", javaBinary.getName()))
          .append(getArgument("srcs", javaBinary.getSrcsList()))
          .append(System.lineSeparator())
          .append(String.format("  main_class = \"%s\",", javaBinary.getMainClass()))
          .append(getArgument("deps", javaBinary.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
      result.append(getCheckstyleTest(checkstyleTests, javaBinary.getName()));
    }
    return result.toString();
  }

  private String getJavaTests(
      List<JavaTest> javaTests, List<CheckstyleTestExtension> checkstyleTests) {
    StringBuilder result = new StringBuilder();
    for (JavaTest javaTest : javaTests) {
      result
          .append("java_test(")
          .append(System.lineSeparator())
          .append(String.format("  name = \"%s\",", javaTest.getName()))
          .append(getArgument("srcs", javaTest.getSrcsList()))
          .append(getArgument("resources", javaTest.getResourcesList()))
          .append(System.lineSeparator())
          .append(String.format("  test_class = \"%s\",", javaTest.getTestClass()))
          .append(getArgument("deps", javaTest.getDepsList()))
          .append(System.lineSeparator())
          .append(")")
          .append(System.lineSeparator())
          .append(System.lineSeparator());
      result.append(getCheckstyleTest(checkstyleTests, javaTest.getName()));
    }
    return result.toString();
  }

  //  resources = ["resources/test_proto.proto"],

  private String getCheckstyleTest(List<CheckstyleTestExtension> checkstyleTests, String target) {
    StringBuilder result = new StringBuilder();
    for (CheckstyleTestExtension checkstyleTest : checkstyleTests) {
      if (checkstyleTest.getTarget().replace(":", "").equals(target)) {
        result
            .append("checkstyle_test(")
            .append(System.lineSeparator())
            .append(String.format("  name = \"%s\",", checkstyleTest.getName()))
            .append(System.lineSeparator())
            .append(String.format("  target = \"%s\",", checkstyleTest.getTarget()))
            .append(System.lineSeparator())
            .append(")")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
        return result.toString();
      }
    }
    log.atWarning().log("Can't find the checkstyle_test for %s target", target);
    return result.toString();
  }

  private String getArgument(String argumentName, List<String> labels) {
    StringBuilder result = new StringBuilder();
    if (!labels.isEmpty()) {
      result.append(System.lineSeparator());
      result.append("  ").append(argumentName).append(" = [");
      if (labels.size() == 1) {
        result.append(String.format("\"%s\"],", labels.get(0)));
      } else {
        result.append(System.lineSeparator());
        for (String dep : labels) {
          result.append(String.format("    \"%s\",", dep));
          result.append(System.lineSeparator());
        }
        result.append("  ],");
      }
    }
    return result.toString();
  }
}

