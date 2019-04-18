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

import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.CheckstyleTestExtension;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaBinary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaGrpcLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaProtoLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaTest;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.ProtoLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

// TODO: Figure out if there's a standard parser we can use to replace this one.
public class BuildFileParser {

  private static final String TEXT_BETWEEN_DOUBLE_QUOTES_REGEX = "\"(.*?)\"";

  private enum Rule {
    proto_library,
    java_library,
    java_binary,
    java_test,
    java_proto_library,
    java_grpc_library,
    checkstyle_test
  }

  private FileUtils fileUtils;

  @Inject
  public BuildFileParser(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public BuildFile getBuildFile(String path) {
    BuildFile.Builder result = BuildFile.newBuilder();
    String fileContent = fileUtils.readFileUnchecked(path);

    List<String> lines =
        Arrays.stream(fileContent.split(System.lineSeparator()))
            .map(String::trim)
            .collect(Collectors.toList());

    for (Rule rule : Rule.values()) {
      for (int i = 0; i < lines.size(); i++) {
        List<String> ruleContent = new ArrayList<>();
        if (lines.get(i).startsWith(rule.name())) {
          while (!lines.get(i).equals(")")) {
            ruleContent.add(lines.get(i));
            i++;
          }
          addRule(result, rule, ruleContent, path);
        }
      }
    }
    return result.build();
  }

  private void addRule(
      BuildFile.Builder builder, Rule rule, List<String> ruleContent, String path) {
    switch (rule) {
      case java_library:
        builder.addJavaLibrary(getJavaLibrary(getRuleAttributes(ruleContent, path)));
        break;
      case proto_library:
        builder.addProtoLibrary(getProtoLibrary(getRuleAttributes(ruleContent, path)));
        break;
      case java_binary:
        builder.addJavaBinary(getJavaBinary(getRuleAttributes(ruleContent, path)));
        break;
      case java_test:
        builder.addJavaTest(getJavaTest(getRuleAttributes(ruleContent, path)));
        break;
      case java_proto_library:
        builder.addJavaProtoLibrary(getJavaProtoLibrary(getRuleAttributes(ruleContent, path)));
        break;
      case java_grpc_library:
        builder.addJavaGrpcLibrary(getJavaGrpcLibrary(getRuleAttributes(ruleContent, path)));
        break;
      case checkstyle_test:
        builder.addCheckstyleTest(getCheckstyleTestExtension(getRuleAttributes(ruleContent, path)));
        break;
      default:
        System.out.println(String.format("%s rule isn't supported.", rule));
    }
  }

  // Returns map with attribute name as key and list of attribute values as the value
  private Map<String, List<String>> getRuleAttributes(List<String> ruleContent, String path) {
    Map<String, List<String>> result = new HashMap<>();

    List<String> ruleAttributes =
        Arrays.asList(
            "name", "srcs", "deps", "main_class", "test_class", "resources", "target", "tags");
    String currentAttribute = "";
    for (int i = 0; i < ruleContent.size(); i++) {
      String line = ruleContent.get(i);
      String firstWordInLine = line.split(" ")[0];

      if (ruleAttributes.contains(firstWordInLine) && !result.containsKey(firstWordInLine)) {
        result.put(firstWordInLine, new ArrayList<>());
        currentAttribute = firstWordInLine;
      }
      if (line.contains("glob(")) {
        StringBuilder globBody = new StringBuilder();
        boolean isGlobBody = true;

        while (isGlobBody) {
          String currentLine = ruleContent.get(i);
          globBody.append(currentLine);
          globBody.append(System.lineSeparator());
          isGlobBody = !currentLine.endsWith("),");
          if (isGlobBody) {
            i++;
          }
        }
        result.put(
            currentAttribute, getFilenamesByGlob(globBody.toString(), path.replace("/BUILD", "")));
        continue;
      }
      if (line.endsWith("[") || line.startsWith("]") || line.startsWith("#")) {
        continue;
      }
      String attributeValue = getSubstringByRegex(line, TEXT_BETWEEN_DOUBLE_QUOTES_REGEX);
      if (!attributeValue.isEmpty()) {
        result
            .get(currentAttribute)
            .add(getSubstringByRegex(line, TEXT_BETWEEN_DOUBLE_QUOTES_REGEX).replace("\"", ""));
      }
    }
    return result;
  }

  // TODO: Support all cases in glob function
  // (https://docs.bazel.build/versions/master/be/functions.html#glob)
  private List<String> getFilenamesByGlob(String globBody, String path) {
    List<String> result = new ArrayList<>();
    List<String> globValues = new ArrayList<>();
    for (String line : globBody.split(System.lineSeparator())) {
      String globValue = getSubstringByRegex(line, TEXT_BETWEEN_DOUBLE_QUOTES_REGEX);
      if (!globValue.isEmpty()) {
        globValues.add(globValue.replace("\"", ""));
      }
    }

    for (String globValue : globValues) {
      String fileExtension = globValue.substring(globValue.lastIndexOf('.') + 1);
      String[] globValueParts = globValue.split("/");
      if (globValueParts.length == 1) {
        result.addAll(getFilesByExtension(path, fileExtension));
      } else {
        String intermediatePaths = path;
        for (String currentGlobValuePart : globValueParts) {
          if (currentGlobValuePart.endsWith("." + fileExtension)) {
            result.addAll(getFilesByExtension(intermediatePaths, fileExtension));
            continue;
          }
          if (currentGlobValuePart.equals("**")) {
            try {
              result.addAll(getFilesByExtension(intermediatePaths, fileExtension));
              List<String> folderPaths =
                  fileUtils
                      .listContentsRecursively(intermediatePaths)
                      .stream()
                      .filter(item -> fileUtils.folderExists(item))
                      .collect(Collectors.toList());

              for (String folderPath : folderPaths) {
                result.addAll(getFilesByExtension(folderPath, fileExtension));
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } else {
            intermediatePaths = fileUtils.joinPaths(intermediatePaths, currentGlobValuePart);
          }
        }
      }
    }
    return result;
  }

  private List<String> getFilesByExtension(String path, String fileExtension) {
    try {
      return fileUtils
          .listContents(path)
          .stream()
          .map(item -> fileUtils.joinPaths(path, item))
          .filter(item -> fileUtils.fileExists(item))
          .filter(file -> file.endsWith("." + fileExtension))
          .map(file -> file.replace(path + "/", ""))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getSubstringByRegex(String line, String regex) {
    String result = "";
    Matcher matcher = Pattern.compile(regex).matcher(line);
    int count = 0;
    if (matcher.find()) {
      result = matcher.group();
      count++;
    }
    if (count != 1) {
      result = "";
    }
    return result;
  }

  private JavaLibrary getJavaLibrary(Map<String, List<String>> attributes) {
    JavaLibrary.Builder result = JavaLibrary.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        case "srcs":
          result.addAllSrcs(values);
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private ProtoLibrary getProtoLibrary(Map<String, List<String>> attributes) {
    ProtoLibrary.Builder result = ProtoLibrary.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        case "srcs":
          result.addAllSrcs(values);
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private JavaBinary getJavaBinary(Map<String, List<String>> attributes) {
    JavaBinary.Builder result = JavaBinary.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        case "srcs":
          result.addAllSrcs(values);
          break;
        case "main_class":
          result.setMainClass(values.get(0));
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private JavaTest getJavaTest(Map<String, List<String>> attributes) {
    JavaTest.Builder result = JavaTest.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        case "srcs":
          result.addAllSrcs(values);
          break;
        case "test_class":
          result.setTestClass(values.get(0));
          break;
        case "resources":
          result.addAllResources(values);
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private JavaProtoLibrary getJavaProtoLibrary(Map<String, List<String>> attributes) {
    JavaProtoLibrary.Builder result = JavaProtoLibrary.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private JavaGrpcLibrary getJavaGrpcLibrary(Map<String, List<String>> attributes) {
    JavaGrpcLibrary.Builder result = JavaGrpcLibrary.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "deps":
          result.addAllDeps(values);
          break;
        case "srcs":
          result.addAllSrcs(values);
          break;
        case "tags":
          result.addAllTags(values);
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }

  private CheckstyleTestExtension getCheckstyleTestExtension(Map<String, List<String>> attributes) {
    CheckstyleTestExtension.Builder result = CheckstyleTestExtension.newBuilder();
    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      List<String> values = entry.getValue();
      switch (entry.getKey()) {
        case "name":
          result.setName(values.get(0));
          break;
        case "target":
          result.setTarget(values.get(0));
          break;
        case "config":
          result.setConfig(values.get(0));
          break;
        default:
          // Do nothing
      }
    }
    return result.build();
  }
}

