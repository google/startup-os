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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.buildfilegenerator.Protos.Import;
import com.google.startupos.tools.buildfilegenerator.Protos.JavaClass;

import javax.inject.Inject;

public class JavaClassAnalyzer {
  private FileUtils fileUtils;

  @Inject
  public JavaClassAnalyzer(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public JavaClass getJavaClass(String filePath) throws IOException {
    JavaClass.Builder result = JavaClass.newBuilder();

    String fileContent = fileUtils.readFile(filePath);
    String classname = getClassname(filePath);
    result.setClassname(classname).setPackage(getPackage(fileContent, classname));
    getImportLines(fileContent).forEach(line -> result.addImport(getImport(line)));

    result.setIsTestClass(isTestClass(fileContent));
    result.setHasMainMethod(hasMainMethod(fileContent));

    return result.build();
  }

  private static String getClassname(String filePath) {
    String[] parts = filePath.split("/");
    return parts[parts.length - 1].replace(".java", "");
  }

  private static String getPackage(String fileContent, String classname) {
    List<String> packageLines = getLinesStartWithKeyword(fileContent, "package", "");
    if (packageLines.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find package for the file: %s", classname));
    }
    if (packageLines.size() > 1) {
      throw new IllegalArgumentException(
          String.format("Found %d packages for the file: %s", packageLines.size(), classname));
    }
    return packageLines.get(0).split(" ")[1].replace(";", "");
  }

  private static List<String> getImportLines(String fileContent) {
    return getLinesStartWithKeyword(fileContent, "import", ".");
  }

  private static List<String> getLinesStartWithKeyword(
      String fileContent, String keyword, String lineShouldContain) {
    return Arrays.stream(fileContent.split(System.lineSeparator()))
        .map(String::trim)
        .collect(Collectors.toList())
        .stream()
        .filter(
            line ->
                line.startsWith(keyword + " ")
                    && line.contains(lineShouldContain)
                    && line.substring(line.length() - 1).equals(";"))
        .collect(Collectors.toList());
  }

  private static Import getImport(String importLine) {
    Import.Builder importBuilder = Import.newBuilder();

    boolean isStaticImport = importLine.split(" ")[1].trim().equals("static");

    List<String> lineParts = Arrays.asList(importLine.replace(";", "").split(" "));
    String importBody = isStaticImport ? lineParts.get(2) : lineParts.get(1);
    List<String> importParts = Arrays.asList(importBody.split("\\."));

    String importedClassName;
    if (isStaticImport) {
      importBuilder.addAllImportDir(importParts.subList(0, importParts.size() - 2));
      importedClassName = importParts.get(importParts.size() - 2);
    } else {
      importBuilder.addAllImportDir(importParts.subList(0, importParts.size() - 1));
      importedClassName = importParts.get(importParts.size() - 1);
    }
    if (importedClassName.equals("*")) {
      importBuilder.setWholePackageImport(true);
    } else {
      importBuilder.setImportedClassName(importedClassName);
    }

    importBuilder.setStandardJavaPackage(isStandardJavaPackage(importParts.get(0)));
    return importBuilder.build();
  }

  private static boolean isStandardJavaPackage(String rootImportDir) {
    List<String> standardJavaPackages = Arrays.asList("java", "javax");
    return standardJavaPackages.contains(rootImportDir);
  }

  private static boolean isTestClass(String fileContent) {
    return fileContent.contains("@Test")
        || fileContent.contains("@Before")
        || fileContent.contains("@BeforeClass")
        || fileContent.contains("@After")
        || fileContent.contains("@AfterClass");
  }

  private static boolean hasMainMethod(String fileContent) {
    return fileContent.contains("public static void main(String[] args)")
        || fileContent.contains("public static void main(String... args)")
        || fileContent.contains("public static void main(String args[])");
  }
}

