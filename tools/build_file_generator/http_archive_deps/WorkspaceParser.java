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
import com.google.startupos.tools.build_file_generator.BuildFileGeneratorUtils;
import com.google.startupos.tools.build_file_generator.Protos.WorkspaceFile;
import com.google.startupos.tools.build_file_generator.Protos.WorkspaceFile.HttpArchive;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class WorkspaceParser {
  private static final String TEXT_BETWEEN_DOUBLE_QUOTES_REGEX = "\"(.*?)\"";
  private FileUtils fileUtils;
  private BuildFileGeneratorUtils buildFileGeneratorUtils;

  @Inject
  public WorkspaceParser(FileUtils fileUtils, BuildFileGeneratorUtils buildFileGeneratorUtils) {
    this.fileUtils = fileUtils;
    this.buildFileGeneratorUtils = buildFileGeneratorUtils;
  }

  public WorkspaceFile getWorkspaceFile() {
    WorkspaceFile.Builder result = WorkspaceFile.newBuilder();
    String absWorkspacePath =
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "WORKSPACE");

    for (HttpArchive httpArchive : getHttpArchives(fileUtils.readFileUnchecked(absWorkspacePath))) {
      result.addHttpArchive(httpArchive);
    }
    return result.build();
  }

  private ImmutableList<HttpArchive> getHttpArchives(String fileContent) {
    ImmutableList.Builder<HttpArchive> result = ImmutableList.builder();

    List<String> lines =
        Arrays.stream(fileContent.split(System.lineSeparator()))
            .map(String::trim)
            .collect(Collectors.toList());

    for (int i = 0; i < lines.size(); i++) {
      HttpArchive.Builder httpArchive = HttpArchive.newBuilder();
      if (lines.get(i).startsWith("http_archive(")) {
        while (!lines.get(i).equals(")")) {
          if (lines.get(i).contains("name = ")) {
            httpArchive.setName(
                buildFileGeneratorUtils
                    .getSubstringByRegex(lines.get(i), TEXT_BETWEEN_DOUBLE_QUOTES_REGEX)
                    .replace("\"", ""));
          }
          if (lines.get(i).contains("strip_prefix = ")) {
            httpArchive.setStripPrefix(
                buildFileGeneratorUtils
                    .getSubstringByRegex(lines.get(i), TEXT_BETWEEN_DOUBLE_QUOTES_REGEX)
                    .replace("\"", ""));
          }
          // TODO: Add supporting several lines for `urls` argument
          if (lines.get(i).contains("url = ") || lines.get(i).contains("urls = ")) {
            httpArchive.addUrls(
                buildFileGeneratorUtils
                    .getSubstringByRegex(lines.get(i), TEXT_BETWEEN_DOUBLE_QUOTES_REGEX)
                    .replace("\"", ""));
          }
          i++;
        }
        result.add(httpArchive.build());
      }
    }
    return result.build();
  }
}

