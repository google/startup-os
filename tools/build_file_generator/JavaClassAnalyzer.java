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

import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.buildfilegenerator.Protos.Import;
import com.google.startupos.tools.buildfilegenerator.Protos.JavaClass;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class JavaClassAnalyzer {
  private FileUtils fileUtils;

  @Inject
  public JavaClassAnalyzer(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public JavaClass getJavaClass(String filePath) throws IOException {
    JavaClass.Builder result = JavaClass.newBuilder();
    result.setClassName(getJavaClassName(filePath));

    String fileContent = fileUtils.readFile(filePath);
    result.setPackage(getPackage(fileContent, result.getClassName()));

    getImportLines(fileContent).forEach(line -> result.addImport(getImport(line)));
    result.setIsTestClass(isTestClass(fileContent)).setHasMainMethod(hasMainMethod(fileContent));

    return result.build();
  }

  private static String getJavaClassName(String filePath) {
    if (!filePath.endsWith(".java")) {
      throw new IllegalArgumentException("Java class must have `.java` extension: " + filePath);
    }
    String[] parts = filePath.split("/");
    return parts[parts.length - 1].replace(".java", "");
  }

  private static List<String> getLinesStartWithKeyword(
      String fileContent, String keyword, String lineShouldContain) {
    return Arrays.stream(fileContent.split(System.lineSeparator()))
        .map(String::trim)
        .filter(
            line ->
                line.startsWith(keyword)
                    && line.contains(lineShouldContain)
                    && line.substring(line.length() - 1).equals(";"))
        .collect(Collectors.toList());
  }

  private static String getPackage(String fileContent, String className) {
    List<String> packageLines = getLinesStartWithKeyword(fileContent, "package", "");
    if (packageLines.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find package for the file: %s", className));
    }
    if (packageLines.size() > 1) {
      throw new IllegalArgumentException(
          String.format("Found %d packages for the file: %s", packageLines.size(), className));
    }
    return packageLines.get(0).split(" ")[1].replace(";", "");
  }

  private static List<String> getImportLines(String fileContent) {
    return getLinesStartWithKeyword(fileContent, "import ", ".");
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
    for (String line : fileContent.split(System.lineSeparator())) {
      if (line.trim().startsWith("@Test")
          || line.startsWith("@Before")
          || line.startsWith("@BeforeClass")
          || line.startsWith("@After")
          || line.startsWith("@AfterClass")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasMainMethod(String fileContent) {
    for (String line : fileContent.split(System.lineSeparator())) {
      if (line.trim().startsWith("public static void main(String[] args)")
          || line.startsWith("public static void main(String... args)")
          || line.startsWith("public static void main(String args[])")) {
        return true;
      }
    }
    return false;
  }
}

