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

import com.google.common.collect.ImmutableMap;
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch.Operation;
import com.google.startupos.common.Protos.TextDiff;
import com.google.startupos.common.Protos.DiffLine;
import com.google.startupos.common.Protos.WordChange;
import com.google.startupos.common.Lists;
import com.google.startupos.common.Lists.Segment;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      List<DiffLine> leftLines,
      List<DiffLine> rightLines,
      int leftDiffLineNumber,
      int rightDiffLineNumber) {
    // Add left placeholders:
    while (rightDiffLineNumber - leftDiffLineNumber > 0) {
      leftLines.add(
          DiffLine.newBuilder()
              .setType(ChangeType.LINE_PLACEHOLDER)
              .setDiffLineNumber(leftDiffLineNumber)
              .build());
      leftDiffLineNumber++;
    }
    // Add right placeholders:
    while (leftDiffLineNumber - rightDiffLineNumber > 0) {
      rightLines.add(
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
    // @@ -0,0 +1,10 @@ (for new file)
    // Remove 1 to be 0-based, so we subtract 1
    // It shouldn't be negative, although for some reason it is, so we truncate at 0.
    return Math.max(Integer.parseInt(hunkHeader.split(" ")[1].substring(1).split(",")[0]) - 1, 0);
  }

  private int getRightStartLine(String hunkHeader) {
    return Math.max(Integer.parseInt(hunkHeader.split(" ")[2].substring(1).split(",")[0]) - 1, 0);
  }

  public TextDiff getTextDiff(String leftText, String rightText, String diffString) {
    System.out.println("********* diffString *************");
    System.out.println(diffString);
    TextDiff.Builder result =
        TextDiff.newBuilder().setLeftFileContents(leftText).setRightFileContents(rightText);
    if (diffString.isEmpty()) {
      return result.build();
    }
    String[] diffLines = diffString.split("\n");
    List<DiffLine> leftLines = new ArrayList();
    List<DiffLine> rightLines = new ArrayList();
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
        fillPlaceholderGap(leftLines, rightLines, leftDiffLineNumber, rightDiffLineNumber);
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
      int codeLineNumber = type == ChangeType.DELETE ? leftCodeLineNumber : rightCodeLineNumber;
      int diffLineNumber = type == ChangeType.DELETE ? leftDiffLineNumber : rightDiffLineNumber;
      DiffLine diffLine =
          DiffLine.newBuilder()
              .setText(text)
              .setType(type)
              .setCodeLineNumber(codeLineNumber)
              .setDiffLineNumber(diffLineNumber)
              .build();
      if (type == ChangeType.DELETE) {
        leftLines.add(diffLine);
        leftCodeLineNumber++;
        leftDiffLineNumber++;
      } else {
        rightLines.add(diffLine);
        rightCodeLineNumber++;
        rightDiffLineNumber++;
      }
    }
    // Fill any last section:
    fillPlaceholderGap(leftLines, rightLines, leftDiffLineNumber, rightDiffLineNumber);
    addWordChanges(leftLines, rightLines);
    result.addAllLeftDiffLine(leftLines);
    result.addAllRightDiffLine(rightLines);
    TextDiff textDiff = result.build();
    System.out.println("*************** TEXT DIFF **************");
    System.out.println(
        textDiff.toBuilder().clearLeftFileContents().clearRightFileContents().build());
    return textDiff;
  }

  private void addWordChanges(List<DiffLine> leftLines, List<DiffLine> rightLines) {
    List<Integer> diffLineNumbers =
        leftLines.stream().map(line -> line.getDiffLineNumber()).collect(Collectors.toList());
    for (Segment segment : Lists.splitToSegments(diffLineNumbers)) {
      addWordChanges(leftLines, rightLines, segment.startIndex(), segment.endIndex());
    }
  }

  // Adds WordChanges, from segmentStart until segmentEnd (inclusive).
  private void addWordChanges(
      List<DiffLine> leftLines, List<DiffLine> rightLines, int segmentStart, int segmentEnd) {
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs =
        diffMatchPatch.diff_main(
            getMultilineText(leftLines, segmentStart, segmentEnd),
            getMultilineText(rightLines, segmentStart, segmentEnd));
    diffMatchPatch.diff_cleanupSemantic(diffs);

    // Split multi-lines
    int leftLineCounter = 0;
    int rightLineCounter = 0;
    int leftCharCounter = 0;
    int rightCharCounter = 0;
    for (DiffMatchPatch.Diff diff : diffs) {
      String[] diffLines = diff.text.split("\n");
      ChangeType type = getChangeType(diff.operation);
      for (int i = 0; i < diffLines.length; i++) {
        if (i > 0) { // This means we had a newline
          leftCharCounter = 0;
          rightCharCounter = 0;
          if (type == ChangeType.DELETE || type == ChangeType.NO_CHANGE) {
            leftLineCounter++;
          }
          if (type == ChangeType.ADD || type == ChangeType.NO_CHANGE) {
            rightLineCounter++;
          }
        }
        String line = diffLines[i];
        int charCounter = type == ChangeType.DELETE ? leftCharCounter : rightCharCounter;
        WordChange wordChange =
            WordChange.newBuilder()
                .setText(line)
                .setStartIndex(charCounter)
                .setEndIndex(charCounter + line.length())
                .setType(type)
                .build();
        if (type == ChangeType.DELETE) {
          int index = segmentStart + leftLineCounter;
          if (!wordChangeIsWholeLine(leftLines.get(index), wordChange)) {
            leftLines.set(
                index, leftLines.get(index).toBuilder().addWordChange(wordChange).build());
          }
        } else if (type == ChangeType.ADD) {
          int index = segmentStart + rightLineCounter;
          if (!wordChangeIsWholeLine(rightLines.get(index), wordChange)) {
            rightLines.set(
                index, rightLines.get(index).toBuilder().addWordChange(wordChange).build());
          }
        }
        if (type == ChangeType.DELETE || type == ChangeType.NO_CHANGE) {
          leftCharCounter += line.length();
        }
        if (type == ChangeType.ADD || type == ChangeType.NO_CHANGE) {
          rightCharCounter += line.length();
        }
      }
    }
  }

  private boolean wordChangeIsWholeLine(DiffLine line, WordChange word) {
    return line.getText().equals(word.getText());
  }

  // endIndex is inclusive - i.e, diffLines at endIndex are used.
  private String getMultilineText(List<DiffLine> diffLines, int startIndex, int endIndex) {
    StringBuilder result = new StringBuilder();
    for (int i = startIndex; i < endIndex; i++) {
      result.append(diffLines.get(i).getText() + "\n");
    }
    // Last one without newline
    result.append(diffLines.get(endIndex).getText());
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

