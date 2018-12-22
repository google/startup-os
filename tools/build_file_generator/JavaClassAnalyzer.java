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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.startupos.tools.buildfilegenerator.Protos.Import;
import com.google.startupos.tools.buildfilegenerator.Protos.JavaClass;

public class JavaClassAnalyzer {
  public static JavaClass getJavaClass(String fileContent) {
    JavaClass.Builder result = JavaClass.newBuilder();

    List<String> importLines = getImportLines(fileContent);
    importLines.forEach(line -> result.addImport(getImport(line)));

    result.setIsTestClass(isTestClass(fileContent));
    result.setHasMainMethod(hasMainMethod(fileContent));

    return result.build();
  }

  private static List<String> getImportLines(String fileContent) {
    return Arrays.stream(fileContent.split(System.lineSeparator()))
        .filter(
            line ->
                line.startsWith("import ")
                    && line.contains(".")
                    && line.substring(line.length() - 1).equals(";"))
        .collect(Collectors.toList());
  }

  private static Import getImport(String importLine) {
    Import.Builder importBuilder = Import.newBuilder();

    boolean isStaticImport = importLine.split(" ")[1].equals("static");

    String lastImportPart;
    List<String> importParts;
    if (isStaticImport) {
      String importBody = importLine.split(" ")[2].replace(";", "");
      importParts = Arrays.asList(importBody.split("\\."));
      importBuilder.addAllSubdir(importParts.subList(1, importParts.size() - 2));
      lastImportPart = importParts.get(importParts.size() - 2);
    } else {
      String importBody = importLine.split(" ")[1].replace(";", "");
      importParts = Arrays.asList(importBody.split("\\."));
      importBuilder.addAllSubdir(importParts.subList(1, importParts.size() - 1));
      lastImportPart = importParts.get(importParts.size() - 1);
    }
    if (lastImportPart.equals("*")) {
      importBuilder.setWholePackageImport(true);
    } else {
      importBuilder.setImportedClassName(lastImportPart);
    }
    String rootDir = importParts.get(0);

    importBuilder.setRootDir(rootDir).setStandardJavaPackage(isStandardJavaPackage(rootDir));
    return importBuilder.build();
  }

  private static boolean isStandardJavaPackage(String rootImportDir) {
    // XXX: Is it needed to create dependencies for classes starts with "javax"?
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
        || fileContent.contains("public static void main(String... args)");
  }
}

