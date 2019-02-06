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
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.CheckstyleTestExtension;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaBinary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaGrpcLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaProtoLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.JavaTest;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile.ProtoLibrary;
import com.google.startupos.tools.build_file_generator.Protos.BuildFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class BuildFileAnalyzer {
  private FileUtils fileUtils;

  @Inject
  public BuildFileAnalyzer(FileUtils fileUtils) {
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
          do {
            ruleContent.add(lines.get(++i));
          } while (!lines.get(i + 1).equals(")"));
          addRule(result, rule, ruleContent);
        }
      }
    }
    return result.build();
  }

  private void addRule(BuildFile.Builder builder, Rule rule, List<String> ruleContent) {
    switch (rule) {
      case java_library:
        builder.addJavaLibrary(getJavaLibrary(getRuleAttributes(ruleContent)));
        break;
      case proto_library:
        builder.addProtoLibrary(getProtoLibrary(getRuleAttributes(ruleContent)));
        break;
      case java_binary:
        builder.addJavaBinary(getJavaBinary(getRuleAttributes(ruleContent)));
        break;
      case java_test:
        builder.addJavaTest(getJavaTest(getRuleAttributes(ruleContent)));
        break;
      case java_proto_library:
        builder.addJavaProtoLibrary(getJavaProtoLibrary(getRuleAttributes(ruleContent)));
        break;
      case java_grpc_library:
        builder.addJavaGrpcLibrary(getJavaGrpcLibrary(getRuleAttributes(ruleContent)));
        break;
      case checkstyle_test:
        builder.addCheckstyleTest(getCheckstyleTestExtension(getRuleAttributes(ruleContent)));
    }
  }

  // Returns map with attribute name as key and  list of attribute values as the value
  private Map<String, List<String>> getRuleAttributes(List<String> ruleContent) {
    Map<String, List<String>> result = new HashMap<>();

    List<String> ruleAttributes =
        Arrays.asList(
            "name", "srcs", "deps", "main_class", "test_class", "resources", "target", "tags");
    for (int i = 0; i < ruleContent.size(); ) {
      String currentAttribute = ruleContent.get(i).split(" ")[0];
      if (ruleAttributes.contains(currentAttribute)) {
        String textBetweenDoubleQuotesRegex = "\"(.*?)\"";

        do {
          String line = ruleContent.get(i);
          if (line.endsWith("[") || line.startsWith("]") || line.startsWith("#")) {
            i++;
            continue;
          }
          if (!result.containsKey(currentAttribute)) {
            result.put(currentAttribute, new ArrayList<>());
          }
          String attributeValue = getSubstringByRegex(line, textBetweenDoubleQuotesRegex);
          if (!attributeValue.isEmpty()) {
            result
                .get(currentAttribute)
                .add(getSubstringByRegex(line, textBetweenDoubleQuotesRegex).replace("\"", ""));
          }
          i++;
        } while (i != ruleContent.size()
            && !ruleAttributes.contains(ruleContent.get(i).split(" ")[0]));
      }
    }
    return result;
  }

  private String getSubstringByRegex(String line, String regex) {
    String result = "";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);
    int count = 0;
    if (matcher.find()) {
      result = matcher.group();
      count++;
    }
    if (count == 0) {
      System.out.println(
          String.format("Can't find any substrings for line: %s. Regex: %s", line, regex));
    }
    if (count > 1) {
      System.out.println(
          String.format("Found more than two substring for line: %s. Regex: %s", line, regex));
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
      }
    }
    return result.build();
  }

  private enum Rule {
    proto_library,
    java_library,
    java_binary,
    java_test,
    java_proto_library,
    java_grpc_library,
    checkstyle_test
  }
}

