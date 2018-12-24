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

    getImportLines(fileContent).forEach(line -> result.addImport(getImport(line)));
    result.setIsTestClass(isTestClass(fileContent)).setHasMainMethod(hasMainMethod(fileContent));

    return result.build();
  }

  private static List<String> getImportLines(String fileContent) {
    return Arrays.stream(fileContent.split(System.lineSeparator()))
        .map(String::trim)
        .filter(
            line ->
                line.startsWith("import ")
                    && line.contains(".")
                    && line.substring(line.length() - 1).equals(";"))
        .collect(Collectors.toList());
  }

  private static Import getImport(String importLine) {
    Import.Builder result = Import.newBuilder();

    // e.g. `import static org.mockito.Mockito.mock;` will be converted to array [static,
    // org.mockito.Mockito.mock]
    String[] importLineParts = importLine.replace("import ", "").replace(";", "").split(" ");

    boolean isStaticImport =
        importLineParts.length > 1 && importLineParts[0].trim().equals("static");
    result.setIsStatic(isStaticImport);

    String[] importBodyParts =
        isStaticImport ? importLineParts[1].split("\\.") : importLineParts[0].split("\\.");
    if (importBodyParts.length < 3) {
      throw new IllegalArgumentException("Import is too broad: " + importLine);
    }
    if (!isStaticImport) {
      result.setWholePackageImport(
          (importBodyParts[importBodyParts.length - 1].equals("*"))
              && (!Character.isUpperCase(importBodyParts[importBodyParts.length - 2].charAt(0))));
    }
    for (String current : importBodyParts) {
      if (!current.equals("*")) {
        if (Character.isUpperCase(current.charAt(0))) {
          result.setClassName(current);
          break;
        }
        result.setPackage(
            result.getPackage().isEmpty() ? current : result.getPackage() + "." + current);
      }
    }
    return result.build();
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

