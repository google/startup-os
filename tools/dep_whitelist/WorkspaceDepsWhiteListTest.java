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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import static org.junit.Assert.assertTrue;

import org.yaml.snakeyaml.Yaml;

public class WorkspaceDepsWhiteListTest {

  private static class WorkspaceElement {
    private String label;
    public Map<String, String> attributes;

    public WorkspaceElement(String label) {
      this.label = label;
      this.attributes = new HashMap<String, String>();
    }

    public void setAttribute(String key, String value) {
      this.attributes.put(key, value);
    }

    public String getLabel() {
      return this.label;
    }

    public String getAttribute(String key) {
      return this.attributes.get(key);
    }
  }

  private List<WorkspaceElement> parseLines(List<String> lines) {
    ArrayList<WorkspaceElement> result = new ArrayList<>();
    WorkspaceElement currentElement = null;
    boolean inAttribute = false;
    for (String line : lines) {
      // System.err.println(String.format("processing line: %s", line));
      if (line.contains("(") && !line.contains(")")) {
        inAttribute = true;
        currentElement = new WorkspaceElement(line.replace("(", ""));
      } else if (inAttribute && line.contains(")")) {
        inAttribute = false;
        result.add(currentElement);
      } else if (inAttribute) {
        String[] kv = line.split("=");
        if (kv.length == 2) {
          // it's indeed key-value
          String key = kv[0].replaceAll("[^a-zA-Z_]", "");
          String value = kv[1].replaceAll("[\\s\\[\\]\\,\"]", "");
          currentElement.setAttribute(key, value);
        }
      }
    }
    return result;
  }

  private Map<String, List<String>> whitelist;
  private List<WorkspaceElement> workspace;

  @Before
  public void setUp() throws Exception {
    whitelist = new Yaml().load(new FileInputStream(new File("whitelist.yaml")));
    try (Stream<String> stream = Files.lines(Paths.get("WORKSPACE"))) {
      workspace = parseLines(stream.collect(Collectors.toList()));
    }
  }

  @Test
  public void gitRepositoriesMatchWhitelist() throws Exception {
    assertTrue(
        "There are non-whitelisted repos in WORKSPACE",
        workspace
            .stream()
            .filter(elem -> elem.getLabel().equals("git_repository"))
            .map(elem -> elem.getAttribute("remote"))
            .allMatch(elem -> whitelist.get("workspace_git_repositories").contains(elem)));
  }
}

