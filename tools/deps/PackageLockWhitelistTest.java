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

package com.google.startupos.tools.dep_whitelist;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class PackageLockWhitelistTest {
  private Map<String, List<String>> whitelist;
  private List<String> packageLockLines;

  private Pattern MAVEN_ARTIFACT_LINE = Pattern.compile("[\\s\\{]+\"artifact\":\\s+\"([^\"]+).*$");

  @Before
  public void setUp() throws Exception {
    whitelist = new Yaml().load(new FileInputStream(new File("whitelist.yaml")));
    packageLockLines = Files.readAllLines(Paths.get("third_party/maven/package-lock.bzl"));
  }

  @Test
  public void packageLockMatchesWhitelist() throws Exception {
    List<String> validPackageGroups = whitelist.get("maven_dependencies");
    boolean depListStarted = false;
    for (String line : packageLockLines) {
      if (line.equals("def list_dependencies():")) {
        depListStarted = true;
        continue;
      }

      if (line.equals("def maven_dependencies(callback = declare_maven)")) {
        break;
      }

      if (depListStarted) {
        Matcher m = MAVEN_ARTIFACT_LINE.matcher(line);
        if (m.matches()) {
          String artifact = m.group(1);

          boolean isValidPackage = false;
          for (String validPackageGroup : validPackageGroups) {
            if (artifact.startsWith(validPackageGroup)) {
              isValidPackage = true;
              break;
            }
          }

          assertTrue(
              String.format("Artifact %s is not in the whitelist", artifact), isValidPackage);
        }
      }
    }
  }
}

