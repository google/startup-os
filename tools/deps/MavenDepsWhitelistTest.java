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

import org.junit.Test;
import org.junit.Before;
import java.util.Map;
import java.util.Set;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import static org.junit.Assert.assertTrue;

import org.yaml.snakeyaml.Yaml;

public class MavenDepsWhitelistTest {
  private Map<String, Map<String, Object>> dependencies;
  private Map<String, List<String>> whitelist;

  @Before
  public void setUp() throws Exception {
    dependencies = new Yaml().load(new FileInputStream(new File("dependencies.yaml")));
    whitelist = new Yaml().load(new FileInputStream(new File("tools/deps/whitelist.yaml")));
  }

  @Test
  public void dependenciesList() throws Exception {
    Set<String> packageGroups = dependencies.get("dependencies").keySet();
    List<String> validPackageGroups = whitelist.get("maven_dependencies");

    for (String packageGroup : packageGroups) {
      boolean isValidPackage = false;
      for (String validPackageGroup : validPackageGroups) {
        if (packageGroup.startsWith(validPackageGroup)) {
          isValidPackage = true;
          break;
        }
      }

      assertTrue(
          String.format("Package group %s is not in the whitelist", packageGroup), isValidPackage);
    }
  }
}

