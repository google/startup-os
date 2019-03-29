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
import com.google.startupos.tools.build_file_generator.Protos.Import;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class JavaClassAnalyzer {
  private static final List<String> JAVA_LANG_CLASSES =
      Arrays.asList(
          "AbstractMethodError",
          "Appendable",
          "ArithmeticException",
          "ArrayIndexOutOfBoundsException",
          "ArrayStoreException",
          "AssertionError",
          "AutoCloseable",
          "Boolean",
          "BootstrapMethodError",
          "Byte",
          "CharSequence",
          "Character",
          "Character.Subset",
          "Character.UnicodeBlock",
          "Character.UnicodeScript",
          "Class",
          "ClassCastException",
          "ClassCircularityError",
          "ClassFormatError",
          "ClassLoader",
          "ClassNotFoundException",
          "ClassValue",
          "CloneNotSupportedException",
          "Cloneable",
          "Comparable",
          "Compiler",
          "Deprecated",
          "Double",
          "Enum",
          "EnumConstantNotPresentException",
          "Error",
          "Exception",
          "ExceptionInInitializerError",
          "Float",
          "FunctionalInterface",
          "IllegalAccessError",
          "IllegalAccessException",
          "IllegalArgumentException",
          "IllegalMonitorStateException",
          "IllegalStateException",
          "IllegalThreadStateException",
          "IncompatibleClassChangeError",
          "IndexOutOfBoundsException",
          "InheritableThreadLocal",
          "InstantiationError",
          "InstantiationException",
          "Integer",
          "InternalError",
          "InterruptedException",
          "Iterable",
          "LinkageError",
          "Long",
          "Math",
          "NegativeArraySizeException",
          "NoClassDefFoundError",
          "NoSuchFieldError",
          "NoSuchFieldException",
          "NoSuchMethodError",
          "NoSuchMethodException",
          "NullPointerException",
          "Number",
          "NumberFormatException",
          "Object",
          "OutOfMemoryError",
          "Override",
          "Package",
          "Process",
          "ProcessBuilder",
          "ProcessBuilder.Redirect",
          "ProcessBuilder.Redirect.Type",
          "Readable",
          "ReflectiveOperationException",
          "Runnable",
          "Runtime",
          "RuntimeException",
          "RuntimePermission",
          "SafeVarargs",
          "SecurityException",
          "SecurityManager",
          "Short",
          "StackOverflowError",
          "StackTraceElement",
          "StrictMath",
          "String",
          "StringBuffer",
          "StringBuilder",
          "StringIndexOutOfBoundsException",
          "SuppressWarnings",
          "System",
          "Thread",
          "Thread.State",
          "Thread.UncaughtExceptionHandler",
          "ThreadDeath",
          "ThreadGroup",
          "ThreadLocal",
          "Throwable",
          "TypeNotPresentException",
          "UnknownError",
          "UnsatisfiedLinkError",
          "UnsupportedClassVersionError",
          "UnsupportedOperationException",
          "VerifyError",
          "VirtualMachineError",
          "Void");

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

    List<String> importedClasses =
        result.getImportList().stream().map(Import::getClassName).collect(Collectors.toList());
    for (String classname : getUsedClassnamesInCode(fileContent)) {
      if (!result.getClassName().equals(classname) && !importedClasses.contains(classname)) {
        result.addUsedClassesFromTheSamePackage(classname);
      }
    }

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
    List<String> packageLines = getLinesStartWithKeyword(fileContent, "package ", "");
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

    List<String> importBodyParts;
    if (isStaticImport) {
      importBodyParts = (Arrays.asList(importLineParts[1].split("\\.")));
    } else {
      importBodyParts = Arrays.asList(importLineParts[0].split("\\."));
    }

    if (!isStaticImport) {
      result.setWholePackageImport(
          (importBodyParts.get(importBodyParts.size() - 1).equals("*"))
              && (!Character.isUpperCase(
                  importBodyParts.get(importBodyParts.size() - 2).charAt(0))));
    }
    for (int i = 0; i < importBodyParts.size(); i++) {
      String current = importBodyParts.get(i);
      if (!current.equals("*")) {
        if (Character.isUpperCase(current.charAt(0))) {
          if (result.getRootClass().isEmpty()) {
            result.setRootClass(current);
          }
          result.setClassName(current);
          if (isStaticImport || importBodyParts.size() - 1 == i) {
            break;
          }
        }
        if (result.getPackage().isEmpty()) {
          result.setPackage(current);
        } else if (Character.isLowerCase(current.charAt(0))) {
          result.setPackage(result.getPackage() + "." + current);
        }
      }
    }

    return result.build();
  }

  private List<String> getUsedClassnamesInCode(String fileContent) {
    // Finds all multiline and javadoc comments which are between `/*` and `*/` or `/**` and `*/`
    final String multilineCommentRegex =
        "((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/";
    // Finds string values in the code which are between double quotes.
    final String stringValueRegex = "\"(.*?)\"";
    List<String> javaCodeLines =
        Arrays.stream(
                fileContent
                    .replaceAll(multilineCommentRegex, "")
                    .replaceAll(stringValueRegex, "")
                    .split(System.lineSeparator()))
            .filter(line -> !line.trim().startsWith("//") && !line.trim().startsWith("import "))
            .collect(Collectors.toList());
    List<String> innerClasses = new ArrayList<>();
    for (String line : javaCodeLines) {
      for (String classname : getJavaClassnames(line)) {
        if (line.contains(" class " + classname)
            || line.contains(" interface " + classname)
            || line.contains(" enum " + classname)) {
          innerClasses.add(classname);
        }
      }
    }
    return getJavaClassnames(javaCodeLines)
        .stream()
        .filter(classname -> !innerClasses.contains(classname))
        .collect(Collectors.toList());
  }

  private List<String> getJavaClassnames(List<String> javaCodeLines) {
    Set<String> result = new HashSet<>();
    for (String line : javaCodeLines) {
      result.addAll(getJavaClassnames(line));
    }
    return ImmutableList.copyOf(result);
  }

  private List<String> getJavaClassnames(String javaCodeLine) {
    Set<String> result = new HashSet<>();
    // Finds all words in CamelCase. We suppose that these words are
    // names of Java classes in the code.
    final String classnameRegex = "\\s([A-Z][a-z0-9]+[a-z0-9A-Z]+)+[\\s|.|[\\(]]";
    Pattern pattern = Pattern.compile(classnameRegex);
    Matcher matcher = pattern.matcher(javaCodeLine.replace("(", "  "));
    while (matcher.find()) {
      String classname = matcher.group().trim().replace(".", "").replace("(", "");
      if (!JAVA_LANG_CLASSES.contains(classname)) {
        result.add(classname);
      }
    }
    return ImmutableList.copyOf(result);
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

