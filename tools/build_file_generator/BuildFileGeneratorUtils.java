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

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.FileUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  public List<String> getFilenamesByGlob(String path, String globLine) {
    List<String> result = new ArrayList<>();
    try {
      for (String globBody : getSubstringsByRegex(globLine, "\\[(.*?)\\]")) {
        for (String pattern : getSubstringsByRegex(globBody, TEXT_BETWEEN_DOUBLE_QUOTES_REGEX)) {
          result.addAll(getFilenamesByGlobPattern(path, pattern.replace("\"", "")));
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ImmutableList<String> getSubstringsByRegex(String line, String regex) {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    Matcher matcher = Pattern.compile(regex).matcher(line.replace(System.lineSeparator(), ""));
    while (matcher.find()) {
      result.add(matcher.group());
    }
    return result.build();
  }

  // TODO: Support `exclude` and `exclude_directories` arguments
  // (https://docs.bazel.build/versions/master/be/functions.html#glob)
  private List<String> getFilenamesByGlobPattern(String path, String pattern) throws IOException {
    if (!fileUtils.folderExists(path)) {
      throw new IllegalStateException(String.format("%s folder does not exist", path));
    }
    Set<String> result = new HashSet<>();
    Set<String> dirsContainBuildFile = new HashSet<>();
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    Files.walkFileTree(
        Paths.get(path),
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            // Relative path under `path`
            String perlacedPatch = file.toString().replace(path, "");
            int slashAmount = perlacedPatch.length() - perlacedPatch.replace("/", "").length();
            if (slashAmount > 1 || (slashAmount == 1 && !pattern.startsWith("**"))) {
              perlacedPatch = perlacedPatch.replaceFirst("/", "");
            }
            if (matcher.matches(Paths.get(perlacedPatch))) {
              String relativeFilename =
                  fileUtils.joinPaths(
                      file.getParent().toString().replace(path, ""), file.getFileName().toString());
              if (relativeFilename.startsWith("/")) {
                relativeFilename = relativeFilename.replaceFirst("/", "");
              }
              result.add(relativeFilename);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult preVisitDirectory(
              Path dir, BasicFileAttributes basicFileAttributes) throws IOException {
            if (fileUtils.listContents(dir.toString()).contains("BUILD")) {
              String relativeDir = (dir.toString()).replace(path, "");
              if (relativeDir.startsWith("/")) {
                relativeDir = relativeDir.replaceFirst("/", "");
              }
              dirsContainBuildFile.add(relativeDir);
            }
            return FileVisitResult.CONTINUE;
          }
        });
    return result
        .stream()
        .filter(
            file -> {
              // Remove results that are siblings of BUILD file
              // https://docs.bazel.build/versions/master/be/functions.html#recursive_glob_example
              if (file.contains("/")) {
                String parentPath = file.substring(0, file.lastIndexOf("/"));
                return !dirsContainBuildFile.contains(parentPath);
              } else {
                return true;
              }
            })
        .collect(Collectors.toList());
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

