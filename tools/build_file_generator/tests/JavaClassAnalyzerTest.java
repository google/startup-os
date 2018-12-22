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

package com.google.startupos.tools.buildfilegenerator.tests;

import com.google.startupos.tools.buildfilegenerator.JavaClassAnalyzer;
import org.junit.Test;

import com.google.startupos.tools.buildfilegenerator.Protos.JavaClass;
import com.google.startupos.tools.buildfilegenerator.Protos.Import;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JavaClassAnalyzerTest {
  @Test
  public void isTestClassTest() {
    String fileContent =
        "package com.test.tests;\n\n"
            + "import org.junit.Test;\n"
            + "import static org.junit.Assert.assertEquals;\n\n"
            + "public class TestClass {\n"
            + "  @Test\n"
            + "  public void test() {\n"
            + "    assertEquals(4, 2+2);\n"
            + "  }\n"
            + "}\n";
    JavaClass expectedJavaClass =
        JavaClass.newBuilder()
            .addImport(
                Import.newBuilder()
                    .setRootDir("org")
                    .addAllSubdir(Arrays.asList("junit"))
                    .setImportedClassName("Test")
                    .build())
            .addImport(
                Import.newBuilder()
                    .setRootDir("org")
                    .addAllSubdir(Arrays.asList("junit"))
                    .setImportedClassName("Assert")
                    .build())
            .setIsTestClass(true)
            .build();
    assertEquals(expectedJavaClass, JavaClassAnalyzer.getJavaClass(fileContent));
  }

  @Test
  public void isWholePackageImportTest() {
    String fileContent =
        "package com.test.tests;\n\n"
            + "import java.util.*;\n\n"
            + "public class TestClass {\n"
            + "  List<Integer> list = new ArrayList<>();\n"
            + "}";
    JavaClass expectedJavaClass =
        JavaClass.newBuilder()
            .addImport(
                Import.newBuilder()
                    .setRootDir("java")
                    .addAllSubdir(Arrays.asList("util"))
                    .setWholePackageImport(true)
                    .setStandardJavaPackage(true)
                    .build())
            .build();
    assertEquals(expectedJavaClass, JavaClassAnalyzer.getJavaClass(fileContent));
  }

  @Test
  public void hasMainMethodTest() {
    String fileContent =
        "package com.test.tests;\n\n"
            + "public class TestClass {\n"
            + "  public static void main(String[] args) {}\n"
            + "}";
    JavaClass expectedJavaClass = JavaClass.newBuilder().setHasMainMethod(true).build();
    assertEquals(expectedJavaClass, JavaClassAnalyzer.getJavaClass(fileContent));
  }
}

