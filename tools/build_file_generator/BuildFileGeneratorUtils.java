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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class BuildFileGeneratorUtils {
  private FileUtils fileUtils;

  public static final String TEXT_BETWEEN_DOUBLE_QUOTES_REGEX = "\"(.*?)\"";

  @Inject
  public BuildFileGeneratorUtils(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  // TODO: Support all cases in glob function
  // (https://docs.bazel.build/versions/master/be/functions.html#glob)
  public List<String> getFilenamesByGlob(String globBody, String path) {
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
        String intermediatePath = "";
        for (String currentGlobValuePart : globValueParts) {
          if (currentGlobValuePart.endsWith("." + fileExtension)) {
            List<String> filenames =
                getFilesByExtension(fileUtils.joinPaths(path, intermediatePath), fileExtension);
            for (String filename : filenames) {
              result.add(fileUtils.joinPaths(intermediatePath, filename));
            }
            continue;
          }
          if (currentGlobValuePart.equals("**")) {
            try {
              result.addAll(
                  getFilesByExtension(fileUtils.joinPaths(path, intermediatePath), fileExtension));
              List<String> folderPaths =
                  fileUtils
                      .listContentsRecursively(fileUtils.joinPaths(path, intermediatePath))
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
            intermediatePath = fileUtils.joinPaths(intermediatePath, currentGlobValuePart);
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

  public String getSubstringByRegex(String line, String regex) {
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

  public String getProjectPackageSuffix(String projectName, String classPackage) {
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
}

