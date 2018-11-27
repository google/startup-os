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

package com.google.startupos.common;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch.Operation;
import com.google.startupos.common.Protos.TextDiff;
import com.google.startupos.common.Protos.DiffLine;
import com.google.startupos.common.Protos.WordChange;
import com.google.startupos.common.repo.Protos.File;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.lang.Math;
import java.lang.StringBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Text differencer for finding the diff between 2 text documents. */
public class TextDifferencer {
  static final Map<Character, ChangeType> CHANGE_TYPE_MAP =
      ImmutableMap.of(
          ' ', ChangeType.NO_CHANGE,
          '+', ChangeType.ADD,
          '-', ChangeType.DELETE);

  @Inject
  public TextDifferencer() {}

  private void fillPlaceholderGap(
      TextDiff.Builder builder, int leftDiffLineNumber, int rightDiffLineNumber) {
    // Add left placeholders:
    while (rightDiffLineNumber - leftDiffLineNumber > 0) {
      builder.addLeftDiffLine(
          DiffLine.newBuilder()
              .setType(ChangeType.LINE_PLACEHOLDER)
              .setDiffLineNumber(leftDiffLineNumber)
              .build());
      leftDiffLineNumber++;
    }
    // Add right placeholders:
    while (leftDiffLineNumber - rightDiffLineNumber > 0) {
      builder.addRightDiffLine(
          DiffLine.newBuilder()
              .setType(ChangeType.LINE_PLACEHOLDER)
              .setDiffLineNumber(rightDiffLineNumber)
              .build());
      rightDiffLineNumber++;
    }
  }

  private int getLeftStartLine(String hunkHeader) {
    // Examples of diff hunk headers:
    // @@ -1 +1 @@
    // @@ -5,83 +5,83 @@
    // Remove 1 to be 0-based, so we subtract 1
    // It shouldn't be negative, although for some reason it is, so we truncate at 0.
    return Math.max(Integer.parseInt(hunkHeader.split(" ")[1].substring(1).split(",")[0]) - 1, 0);
  }

  private int getRightStartLine(String hunkHeader) {
    return Math.max(Integer.parseInt(hunkHeader.split(" ")[2].substring(1).split(",")[0]) - 1, 0);
  }

  public TextDiff getTextDiff(String leftText, String rightText, String diffString) {
    // System.out.println("********* diffString *************");
    // System.out.println(diffString);
    TextDiff.Builder result =
        TextDiff.newBuilder().setLeftFileContents(leftText).setRightFileContents(rightText);
    if (diffString.isEmpty()) {
      return result.build();
    }
    String[] diffLines = diffString.split("\n");
    int leftCodeLineNumber = getLeftStartLine(diffLines[0]);
    int leftDiffLineNumber = leftCodeLineNumber;
    int rightCodeLineNumber = getRightStartLine(diffLines[0]);
    int rightDiffLineNumber = rightCodeLineNumber;
    for (int diffIndex = 1; diffIndex < diffLines.length; diffIndex++) {
      if (diffLines[diffIndex].charAt(0) == '\\') {
        // This is not a real code line, probably "\ No newline at end of file".
        continue;
      }
      ChangeType type = CHANGE_TYPE_MAP.get(diffLines[diffIndex].charAt(0));
      if (type == null) {
        throw new IllegalStateException(
            "Diff line "
                + diffIndex
                + " does not start with a diff character (+- ):\n"
                + diffLines[diffIndex]
                + "\nFor diffString:\n"
                + diffString);
      }
      if (type == ChangeType.NO_CHANGE) {
        // On NO_CHANGE lines, we know that both diff line indices should be the same. We add
        // placeholder DiffLines to fill in the gaps:
        fillPlaceholderGap(result, leftDiffLineNumber, rightDiffLineNumber);
        // Now gap is filled, so set both line numbers to the maximum:
        leftDiffLineNumber = Math.max(leftDiffLineNumber, rightDiffLineNumber);
        rightDiffLineNumber = leftDiffLineNumber;
        // On to the next line:
        leftCodeLineNumber++;
        rightCodeLineNumber++;
        leftDiffLineNumber++;
        rightDiffLineNumber++;
        continue;
      }
      String text = diffLines[diffIndex].substring(1);
      int codeLineNumber = type == ChangeType.ADD ? rightCodeLineNumber : leftCodeLineNumber;
      int diffLineNumber = type == ChangeType.ADD ? rightDiffLineNumber : leftDiffLineNumber;
      DiffLine diffLine =
          DiffLine.newBuilder()
              .setText(text)
              .setType(type)
              .setCodeLineNumber(codeLineNumber)
              .setDiffLineNumber(diffLineNumber)
              .build();
      if (type == ChangeType.ADD) {
        result.addRightDiffLine(diffLine);
        rightCodeLineNumber++;
        rightDiffLineNumber++;
      } else {
        result.addLeftDiffLine(diffLine);
        leftCodeLineNumber++;
        leftDiffLineNumber++;
      }
    }
    // Fill any last section:
    fillPlaceholderGap(result, leftDiffLineNumber, rightDiffLineNumber);
    TextDiff textDiff = result.build();
    //addWordChanges(textDiff);
    System.out.println("*************** TEXT DIFF **************");
    System.out.println(textDiff);
    return textDiff;
  }

  private List<DiffMatchPatch.Diff> getDiffPatchMatchDiff(String leftText, String rightText) {
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(leftText, rightText);
    diffMatchPatch.diff_cleanupSemantic(diffs);
    return diffs;
  }

  // TODO: remove this and use only DiffLines once front-end is changed.
  private TextDiff addWordChanges(TextDiff textDiff) {
    if (textDiff.getLeftDiffLineList().isEmpty()) {
      return textDiff;
    }
    System.out.println("AAAAAAAAAAAAA");
    TextDiff.Builder result = textDiff.toBuilder();
    result.clearLeftDiffLine();
    result.clearRightDiffLine();
    int segmentStart = 0;
    for (int diffIndex = 1; diffIndex < textDiff.getLeftDiffLineList().size(); diffIndex++) {
      DiffLine leftDiffLine = textDiff.getLeftDiffLineList().get(diffIndex);
      DiffLine prevLeftDiffLine = textDiff.getLeftDiffLineList().get(diffIndex);
      if (leftDiffLine.getDiffLineNumber() - prevLeftDiffLine.getDiffLineNumber() > 1) {
        String leftText = getMultilineText(textDiff.getLeftDiffLineList(), segmentStart, diffIndex);
        String rightText = getMultilineText(textDiff.getRightDiffLineList(), segmentStart, diffIndex);
        List<DiffMatchPatch.Diff> diffs = getDiffPatchMatchDiff(leftText, rightText);
        for (DiffMatchPatch.Diff diff : diffs) {
          System.out.println("WWWWWWWWWWW 1:" + diff.text);
          System.out.println("WWWWWWWWWWWW 1:" + getChangeType(diff.operation));
        }
        segmentStart = diffIndex;
      }
    }
    // Take care of last segment
    if (segmentStart < textDiff.getLeftDiffLineList().size() - 1) {
      ImmutableList<DiffMatchPatch.Diff> diffs = splitMultiLines(getDiffPatchMatchDiff(
          getMultilineText(textDiff.getLeftDiffLineList(), segmentStart, textDiff.getLeftDiffLineList().size()),
          getMultilineText(textDiff.getRightDiffLineList(), segmentStart, textDiff.getRightDiffLineList().size())));
      for (int i = 0; i < diffs.size(); i++) {
        DiffMatchPatch.Diff diff = diffs.get(i);
        System.out.println("WWWWWWWWWWWW 2.1:\n" + diff.text);
        System.out.println("WWWWWWWWWWWW 2.2:" + getChangeType(diff.operation));
      }
    }
     
      //result.addLeftChange(convertToTextChange(diffLine));
    
    return result.build();
  }

  private ImmutableList<DiffMatchPatch.Diff> splitMultiLines(List<DiffMatchPatch.Diff> diffs) {
    ImmutableList.Builder<DiffMatchPatch.Diff> result = ImmutableList.builder();
    for (DiffMatchPatch.Diff diff : diffs) {
      for (String line : diff.text.split("\n")) {
        if (!line.isEmpty()) {
          result.add(new DiffMatchPatch.Diff(diff.operation, line));
        }
      }
    }
    return result.build();
  }

  // endIndex is not inclusive - i.e, diffLines at endIndex is not used.
  private String getMultilineText(List<DiffLine> diffLines, int startIndex, int endIndex) {
    StringBuilder result = new StringBuilder();
    for (int i = startIndex; i < endIndex - 1; i++) {
      result.append(diffLines.get(i).getText() + "\n");
    }
    // Last one without newline
    result.append(diffLines.get(endIndex - 1).getText());
    return result.toString();
  }

  private ChangeType getChangeType(Operation operation) {
    if (operation == Operation.EQUAL) {
      return ChangeType.NO_CHANGE;
    } else if (operation == Operation.INSERT) {
      return ChangeType.ADD;
    } else if (operation == Operation.DELETE) {
      return ChangeType.DELETE;
    } else {
      throw new IllegalArgumentException("Unknown Operation enum: " + operation);
    }
  }
}
