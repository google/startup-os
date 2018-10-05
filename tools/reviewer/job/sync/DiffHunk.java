/*
 * Copyrit 2018 The StartupOS Authors.
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

public class DiffHunk {
  private int baseStartLine;
  private int baseCountLines;
  private int headStartLine;
  private int headCountLines;
  private boolean isNewFileComment;
  private boolean isCommentToTheFirstLine;
  private boolean isFileInHeadHasDifferentNumberOfLines;
  private String hunkBody;

  DiffHunk(String diffHunk) {
    // We are parsing `diff_hunk`.
    // E.g we have a `diff_hunk`: "@@ -57,56 +57,32 @@ code line1 \n ... code lineN \n+"
    // "-57,56" is related to base,
    // "+57,32" is related to head.
    //  "code line1 \n ... code lineN \n+" is hunkBody
    baseStartLine = Integer.parseInt(diffHunk.split(" ")[1].substring(1).split(",")[0]);
    baseCountLines = Integer.parseInt(diffHunk.split(" ")[1].substring(1).split(",")[1]);
    headStartLine = Integer.parseInt(diffHunk.split(" ")[2].substring(1).split(",")[0]);
    headCountLines = Integer.parseInt(diffHunk.split(" ")[2].substring(1).split(",")[1]);
    hunkBody = setHunkBody(diffHunk);

    isNewFileComment = (baseStartLine == 0) && (baseCountLines == 0);
    isCommentToTheFirstLine = baseStartLine == 1 && headStartLine == 1;
    isFileInHeadHasDifferentNumberOfLines = baseCountLines != headCountLines;
  }

  private String setHunkBody(String diffHunk) {
    String[] parts = diffHunk.split("@@\\s[-+]\\d+[\\,]\\d+\\s[-+]\\d+[\\,]\\d+\\s@@");
    return (parts.length > 1) ? parts[1] : "";
  }

  int getBaseStartLine() {
    return baseStartLine;
  }

  int getBaseCountLines() {
    return baseCountLines;
  }

  int getHeadStartLine() {
    return headStartLine;
  }

  int getHeadCountLines() {
    return headCountLines;
  }

  boolean isNewFileComment() {
    return isNewFileComment;
  }

  boolean isCommentToTheFirstLine() {
    return isCommentToTheFirstLine;
  }

  boolean isFileInHeadHasDifferentNumberOfLines() {
    return isFileInHeadHasDifferentNumberOfLines;
  }

  String getHunkBody() {
    return hunkBody;
  }
}

