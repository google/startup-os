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

public class DiffPatch {
  private static final String NEW_LINES_PATTERN = "\\n\\s|\\n\\+|\\n\\-|\\n";
  private static final String DIFF_HUNKS_PATTERN =
      "@@\\s[-+]\\d+[\\,]\\d+\\s[-+]\\d+[\\,]\\d+\\s@@";

  private List<String> newLineSymbols;
  private List<DiffHunk> diffHunks;

  public DiffPatch(String diffPatchStr) {
    newLineSymbols = setNewLineSymbols(diffPatchStr);
    diffHunks = setDiffHunks(diffPatchStr);
  }

  private List<String> setNewLineSymbols(String diffPatchStr) {
    List<String> result = new ArrayList<>();
    Pattern pattern = Pattern.compile(NEW_LINES_PATTERN);
    Matcher matcher = pattern.matcher(diffPatchStr);
    while (matcher.find()) {
      result.add(matcher.group());
    }
    return result;
  }

  private List<DiffHunk> setDiffHunks(String diffPatchStr) {
    List<DiffHunk> result = new ArrayList<>();
    Pattern pattern2 = Pattern.compile(DIFF_HUNKS_PATTERN);
    Matcher matcher2 = pattern2.matcher(diffPatchStr);
    while (matcher2.find()) {
      result.add(new DiffHunk(matcher2.group()));
    }
    return result;
  }

  public List<String> getNewLineSymbols() {
    return newLineSymbols;
  }

  public List<DiffHunk> getDiffHunks() {
    return diffHunks;
  }

  class DiffHunk {
    private int leftStartLine;
    private int rightStartLine;

    DiffHunk(String diffHunkStr) {
      leftStartLine = Integer.parseInt(diffHunkStr.split(" ")[1].substring(1).split(",")[0]);
      rightStartLine = Integer.parseInt(diffHunkStr.split(" ")[2].substring(1).split(",")[0]);
    }

    public int getLeftStartLine() {
      return leftStartLine;
    }

    public int getRightStartLine() {
      return rightStartLine;
    }
  }
}

