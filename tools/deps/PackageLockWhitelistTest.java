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

package com.google.startupos.tools.deps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.util.JsonFormat;
import com.google.startupos.tools.deps.Protos.Dependency;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

/*
 * This test assumes all used Maven artifacts are whitelisted
 * A line not starting with # sign and containing :// is assumed a URL
 */
public class PackageLockWhitelistTest {
  private Map<String, List<String>> whitelist;
  private List<String> packageLockLines;
  private List<Dependency> parsedDependencies;

  private String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2/";

  @Before
  public void setUp() throws Exception {
    whitelist = new Yaml().load(new FileInputStream(new File("tools/deps/whitelist.yaml")));
    packageLockLines = Files.readAllLines(Paths.get("third_party/maven/package-lock.bzl"));
    parsedDependencies = parseDependencies();
  }

  private List<Dependency> parseDependencies() throws Exception {
    List<Dependency> parsedDependencies = new ArrayList();
    boolean depListStarted = false;
    for (String line : packageLockLines) {
      if (!line.startsWith("#") && line.contains("://")) {
        Dependency.Builder message = Dependency.newBuilder();
        JsonFormat.parser().merge(line, message);
        parsedDependencies.add(message.build());
      }
    }
    return parsedDependencies;
  }

  @Test
  public void validateDependencies() throws Exception {
    assertFalse("Parsed dependencies list should not be empty", parsedDependencies.isEmpty());

    List<String> validPackageGroups = whitelist.get("maven_dependencies");
    for (Dependency dep : parsedDependencies) {

      assertEquals("Artifact %s is not in Maven Central", MAVEN_CENTRAL_URL, dep.getRepository());

      boolean isValidPackage = false;
      for (String validPackageGroup : validPackageGroups) {
        if (dep.getArtifact().startsWith(validPackageGroup)) {
          isValidPackage = true;
          break;
        }
      }

      assertTrue(
          String.format("Artifact %s is not in the whitelist", dep.getArtifact()), isValidPackage);
    }
  }
}

