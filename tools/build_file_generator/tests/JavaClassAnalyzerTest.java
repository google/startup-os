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

package com.google.startupos.tools.build_file_generator.tests;

import static org.junit.Assert.assertEquals;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.build_file_generator.JavaClassAnalyzer;
import com.google.startupos.tools.build_file_generator.Protos.Import;
import com.google.startupos.tools.build_file_generator.Protos.JavaClass;
import dagger.Component;
import java.io.IOException;
import java.nio.file.Files;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;

public class JavaClassAnalyzerTest {
  private FileUtils fileUtils;
  private String testFolder;
  private JavaClassAnalyzer javaClassAnalyzer;

  @Before
  public void setup() throws IOException {
    fileUtils = DaggerJavaClassAnalyzerTest_TestComponent.create().getFileUtils();
    testFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    javaClassAnalyzer = new JavaClassAnalyzer(fileUtils);
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface TestComponent {
    FileUtils getFileUtils();
  }

  @Test
  public void isTestClassTest() throws IOException {
    String fileContent =
        "package com.test.tests;"
            + System.lineSeparator()
            + "import org.junit.Test;"
            + System.lineSeparator()
            + "import static org.junit.Assert.assertEquals;"
            + System.lineSeparator()
            + "public class TestClass {"
            + System.lineSeparator()
            + "  @Test"
            + System.lineSeparator()
            + "  public void test() {"
            + System.lineSeparator()
            + "    assertEquals(4, 2+2);"
            + System.lineSeparator()
            + "  }"
            + System.lineSeparator()
            + "}";

    String filePath = fileUtils.joinToAbsolutePath(testFolder, "TestClass.java");
    fileUtils.writeStringUnchecked(fileContent, filePath);

    JavaClass expectedJavaClass =
        JavaClass.newBuilder()
            .setPackage("com.test.tests")
            .addImport(
                Import.newBuilder()
                    .setPackage("org.junit")
                    .setClassName("Test")
                    .setRootClass("Test")
                    .build())
            .addImport(
                Import.newBuilder()
                    .setPackage("org.junit")
                    .setClassName("Assert")
                    .setIsStatic(true)
                    .setRootClass("Assert")
                    .build())
            .setClassName("TestClass")
            .setIsTestClass(true)
            .build();

    assertEquals(expectedJavaClass, javaClassAnalyzer.getJavaClass(filePath));
  }

  @Test
  public void isWholePackageImportTest() throws IOException {
    String fileContent =
        "package com.test.tests;"
            + System.lineSeparator()
            + "import java.util.*;"
            + System.lineSeparator()
            + "public class SomeClass {"
            + System.lineSeparator()
            + "  List<Integer> list = new ArrayList<>();"
            + System.lineSeparator()
            + "}";
    String filePath = fileUtils.joinToAbsolutePath(testFolder, "SomeClass.java");
    fileUtils.writeStringUnchecked(fileContent, filePath);

    JavaClass expectedJavaClass =
        JavaClass.newBuilder()
            .setPackage("com.test.tests")
            .addImport(
                Import.newBuilder().setPackage("java.util").setWholePackageImport(true).build())
            .setClassName("SomeClass")
            .build();

    assertEquals(expectedJavaClass, javaClassAnalyzer.getJavaClass(filePath));
  }

  @Test
  public void hasMainMethodTest() throws IOException {
    String fileContent =
        "package com.test.tests;"
            + System.lineSeparator()
            + "public class SomeClass {"
            + System.lineSeparator()
            + "  public static void main(String[] args) {}"
            + System.lineSeparator()
            + "}";
    String filePath = fileUtils.joinToAbsolutePath(testFolder, "SomeClass.java");
    fileUtils.writeStringUnchecked(fileContent, filePath);

    JavaClass expectedJavaClass =
        JavaClass.newBuilder()
            .setPackage("com.test.tests")
            .setClassName("SomeClass")
            .setHasMainMethod(true)
            .build();

    assertEquals(expectedJavaClass, javaClassAnalyzer.getJavaClass(filePath));
  }
}

