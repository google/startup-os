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

package com.google.startupos.tools.reviewer.job.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses `patch` of GitHub Pull Request file. `Patch` of GitHub Pull Request file can involve
 * one or more parts.
 */
public class GithubPatch {
  /*
   * The regex pattern for all newline symbols search: `\\n\\s` - newline with space, `\\n\\+` -
   * newline with `+` character, `\\n\\-` - newline with `-` character, `\\n` - just newline
   */
  private static final String NEW_LINES_PATTERN = "\\n\\s|\\n\\+|\\n\\-|\\n";
  // The regex pattern for DiffHunkHeader headers search. E.g. `@@ -99,6 +108,16 @@`
  private static final String DIFF_HUNKS_PATTERN = "@@\\s[-]\\d+[\\,\\d]*\\s[+]\\d+[\\,\\d]*\\s@@";

  private List<String> newlineSymbols;
  private List<DiffHunkHeader> diffHunkHeaders;

  /**
   * GithubPatch class constructor.
   *
   * @param diffPatchStr `patch`(diff) of GitHub Pull Request file in string format
   */
  public GithubPatch(String diffPatchStr) {
    newlineSymbols = getMatches(diffPatchStr, NEW_LINES_PATTERN);
    diffHunkHeaders =
        getMatches(diffPatchStr, DIFF_HUNKS_PATTERN)
            .stream()
            .map(DiffHunkHeader::new)
            .collect(Collectors.toList());
  }

  private List<String> getMatches(String value, String pattern) {
    List<String> result = new ArrayList<>();
    Matcher matcher = Pattern.compile(pattern).matcher(value);
    while (matcher.find()) {
      result.add(matcher.group());
    }
    return result;
  }

  public List<String> getNewlineSymbols() {
    return newlineSymbols;
  }

  public List<DiffHunkHeader> getDiffHunkHeaders() {
    return diffHunkHeaders;
  }

  /**
   * A diff hunk header.
   *
   * <p>A part of GitHub Pull Request file's `patch` contains header and body.
   *
   * <p>Header starts and ends with double `@` characters. E.g. `@@ -99,6 +108,16 @@`
   *
   * <p>`-96,6` relates to left side(base): `96` is line number in the file where diff hunk starts,
   * `6` - amount of lines in the DiffHunk.
   *
   * <p>`+108,16` relates to the right side(head): '108' is line number in the file where diff hunk
   * starts, 16 - amount of lines in the diff hunk.
   */
  class DiffHunkHeader {
    // Line number in the file where diff hunk starts on the left side
    private int leftStartLine;
    // Line number in the file where diff hunk starts on the right side
    private int rightStartLine;

    /**
     * DiffHunkHeader class constructor.
     *
     * @param diffHunkHeaderStr a header of diff hunk in string format
     */
    DiffHunkHeader(String diffHunkHeaderStr) {
      try {
        // Parses diff hunk header and gets line number where diff hunk starts on the left and on
        // the
        // right side
        leftStartLine =
            Integer.parseInt(diffHunkHeaderStr.split(" ")[1].substring(1).split(",")[0]);
        rightStartLine =
            Integer.parseInt(diffHunkHeaderStr.split(" ")[2].substring(1).split(",")[0]);
      } catch (RuntimeException e) {
        throw new IllegalArgumentException("Can not parse: " + diffHunkHeaderStr);
      }
    }

    public int getLeftStartLine() {
      return leftStartLine;
    }

    public int getRightStartLine() {
      return rightStartLine;
    }
  }
}

