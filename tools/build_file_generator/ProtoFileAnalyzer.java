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
import com.google.startupos.tools.buildfilegenerator.Protos.ProtoFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class ProtoFileAnalyzer {
  private FileUtils fileUtils;

  @Inject
  public ProtoFileAnalyzer(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public ProtoFile getProtoFile(String filePath) throws IOException {
    ProtoFile.Builder result = ProtoFile.newBuilder();

    String fileContent = fileUtils.readFile(filePath);

    return result
        .setPackage(getPackage(fileContent))
        .setFileName(getProtoFileName(filePath))
        .setJavaOuterClassname(getJavaOuterClassName(fileContent))
        .setJavaPackage(getJavaPackage(fileContent))
        .addAllMessages(getMessages(fileContent))
        .addAllServices(getServices(fileContent))
        .addAllEnums(getEnums(fileContent))
        .build();
  }

  private static String getPackage(String fileContent) {
    List<String> packageLines = getLinesStartWith(fileContent, "package", "");
    if (packageLines.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find package for the file: %s", fileContent));
    }
    if (packageLines.size() > 1) {
      throw new IllegalArgumentException(
          String.format("Found %d packages for the file: %s", packageLines.size(), fileContent));
    }
    return packageLines.get(0).split(" ")[1].replace(";", "");
  }

  private static List<String> getLinesStartWith(
      String fileContent, String keyword, String lineShouldContain) {
    return Arrays.stream(fileContent.split(System.lineSeparator()))
        .map(String::trim)
        .filter(line -> line.startsWith(keyword) && line.contains(lineShouldContain))
        .collect(Collectors.toList());
  }

  private static String getProtoFileName(String filePath) {
    if (!filePath.endsWith(".proto")) {
      throw new IllegalArgumentException("Proto file must have `.proto` extension: " + filePath);
    }
    String[] parts = filePath.split("/");
    return parts[parts.length - 1].replace(".proto", "");
  }

  private static String getJavaOuterClassName(String fileContent) {
    return getOptionValue(fileContent, "java_outer_classname");
  }

  private static String getJavaPackage(String fileContent) {
    return getOptionValue(fileContent, "java_package");
  }

  private static String getOptionValue(String fileContent, String option) {
    List<String> javaPackageLines = getLinesStartWith(fileContent, "option " + option, "=");
    if (javaPackageLines.isEmpty()) {
      return "";
    }
    if (javaPackageLines.size() > 1) {
      throw new IllegalArgumentException(
          String.format("Proto file can have only single `%s` option: %s", option, fileContent));
    }
    String[] javaPackageLineParts = javaPackageLines.get(0).split(" ");
    return javaPackageLineParts[javaPackageLineParts.length - 1].replace(";", "").replace("\"", "");
  }

  private static List<String> getMessages(String fileContent) {
    return getProtoObjectValue(fileContent, ProtoObjectType.MESSAGE);
  }

  private static List<String> getServices(String fileContent) {
    return getProtoObjectValue(fileContent, ProtoObjectType.SERVICE);
  }

  private static List<String> getEnums(String fileContent) {
    return getProtoObjectValue(fileContent, ProtoObjectType.ENUM);
  }

  private static List<String> getProtoObjectValue(String fileContent, ProtoObjectType type) {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    List<String> lines = getLinesStartWith(fileContent, type.value, "");
    for (String line : lines) {
      String[] lineParts =
          line.replace("{", "").replace("}", "").replace(type.value, "").trim().split(" ");
      if (lineParts.length == 1 && Character.isUpperCase(lineParts[0].charAt(0))) {
        result.add(lineParts[0]);
      } else {
        for (String linePart : lineParts) {
          if (Character.isUpperCase(linePart.charAt(0))) {
            result.add(linePart);
            break;
          }
        }
      }
    }
    return result.build();
  }

  private enum ProtoObjectType {
    MESSAGE("message"),
    SERVICE("service"),
    ENUM("enum");

    private final String value;

    ProtoObjectType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}

