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

import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.FileInputStream;
import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.yaml.snakeyaml.Yaml;

public class WorkspaceDepsWhitelistTest {
  private Map<String, List<String>> whitelist;
  private List<String> workspace;
  private static Set<String> keysToValidate =
      new HashSet<String>() {
        {
          add("url");
          add("urls");
          add("remote");
          add("artifact");
        }
      };

  @Before
  public void setUp() throws Exception {
    whitelist = new Yaml().load(new FileInputStream(new File("whitelist.yaml")));
    try (Stream<String> stream = Files.lines(Paths.get("WORKSPACE"))) {
      workspace = stream.collect(Collectors.toList());
    }
  }

  @Test
  public void urlsMatchWhitelist() throws Exception {
    List<String> validUrls = whitelist.get("workspace_dependencies");

    for (String line : workspace) {
      if (line.contains("=")) {
        String[] kv = line.split("=");
        assertTrue(String.format("Line %s should contain key and value", line), kv.length == 2);
        boolean isValidUrl = false;

        String key = kv[0].trim();
        String value = kv[1].replaceAll("[\\s\\[\\]\\,\"]", "");
        if (keysToValidate.contains(key)) {

          for (String validUrl : validUrls) {
            if (value.startsWith(validUrl)) {
              isValidUrl = true;
              break;
            }
          }

          assertTrue(String.format("URL %s is not in the whitelist", value), isValidUrl);
        } else {
          assertFalse(
              String.format("Value at key %s should not contain ://", key), value.contains("://"));
        }
      }
    }
  }
}

