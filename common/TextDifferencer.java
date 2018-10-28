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
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch.Operation;
import com.google.startupos.common.Protos.TextDiff;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.DiffPatchMatchChange;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import java.lang.StringBuilder;

/** Text differencer for finding the diff between 2 text documents. */
public class TextDifferencer {

  @Inject
  public TextDifferencer() {}

  private DiffPatchMatchChange convertToProto(DiffMatchPatch.Diff diff) {
    return DiffPatchMatchChange.newBuilder()
        .setText(diff.text)
        .setType(getChangeType(diff.operation))
        .build();
  }

  private ImmutableList<DiffPatchMatchChange> getDiffPatchMatchChanges(
      String leftContents, String rightContents) {
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(leftContents, rightContents);
    diffMatchPatch.diff_cleanupSemantic(diffs);
    List<DiffPatchMatchChange> result = new ArrayList<DiffPatchMatchChange>();
    ChangeType previousChangeType = ChangeType.NO_CHANGE;
    for (DiffMatchPatch.Diff diff : diffs) {
      DiffPatchMatchChange change = convertToProto(diff);
      if (change.getType() == ChangeType.NO_CHANGE
          || previousChangeType == ChangeType.NO_CHANGE
          || previousChangeType == change.getType()) {
        result.add(change);
      } else {
        // Change last change to REPLACE:
        DiffPatchMatchChange.Builder replaceChange =
            makeReplaceChange(result.get(result.size() - 1)).toBuilder();
        if (change.getType() == ChangeType.ADD) {
          replaceChange.setReplacingText(replaceChange.getReplacingText() + change.getText());
        } else { // ChangeType.DELETE
          replaceChange.setText(replaceChange.getText() + change.getText());
        }
        result.set(result.size() - 1, replaceChange.build());
      }
      previousChangeType = change.getType();
    }
    return ImmutableList.copyOf(result);
  }

  private DiffPatchMatchChange makeReplaceChange(DiffPatchMatchChange change) {
    DiffPatchMatchChange.Builder replaceChange = change.toBuilder();
    if (change.getType() == ChangeType.ADD) {
      // Move text to replacing_text field
      replaceChange.setText("");
      replaceChange.setReplacingText(change.getText());
    } else { // ChangeType.DELETE or ChangeType.REPLACE
      // Do nothing, text is already in the correct field
    }
    replaceChange.setType(ChangeType.REPLACE);
    return replaceChange.build();
  }

  private ImmutableList<TextChange> getSingleSideChanges(
      ImmutableList<DiffPatchMatchChange> changes, ChangeType sideOperation) {
    if (changes.isEmpty()) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<TextChange> result = ImmutableList.builder();
    StringBuilder sb = new StringBuilder();
    int lineNumber = 0;
    int globalIndex = 0;
    int lineIndex = 0;
    int nextGlobalStartIndex = 0;

    for (DiffPatchMatchChange change : changes) {
      ChangeType changeType = change.getType();
      String changeText = change.getText();
      if (sideOperation == ChangeType.ADD && change.getType() == ChangeType.REPLACE) {
        changeText = change.getReplacingText();
      }
      if (change.getType() == ChangeType.REPLACE) {
        // REPLACE acts as a "joker" - it's like both ADD and DELETE. We set it here to
        // sideOperation since it simplifies the code instead of checking for both every time.
        changeType = sideOperation;
      }
      for (int i = 0; i < changeText.length(); i++) {
        char currentChar = changeText.charAt(i);
        if (currentChar == '\n') {
          if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
            result.add(
                TextChange.newBuilder()
                    .setText(sb.toString())
                    .setType(changeType)
                    .setLineNumber(lineNumber)
                    .setGlobalStartIndex(nextGlobalStartIndex)
                    .setGlobalEndIndex(globalIndex)
                    .setStartIndex(lineIndex - sb.toString().length())
                    .setEndIndex(lineIndex)
                    .build());
            // We add 1 to skip the newline
            nextGlobalStartIndex = globalIndex + 1;
          } else {
            result.add(
                TextChange.newBuilder()
                    .setType(ChangeType.LINE_PLACEHOLDER)
                    .setLineNumber(lineNumber)
                    .build());
          }
          lineNumber++;
          sb = new StringBuilder();
          lineIndex = 0;
        } else { // char != '/n'
          if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
            lineIndex++;
          }
          sb.append(currentChar);
        }
        if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
          globalIndex++;
        }
      }
      // Check for leftover from last line
      if (sb.length() > 0) {
        if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
          result.add(
              TextChange.newBuilder()
                  .setText(sb.toString())
                  .setType(changeType)
                  .setLineNumber(lineNumber)
                  .setGlobalStartIndex(nextGlobalStartIndex)
                  .setGlobalEndIndex(globalIndex)
                  .setStartIndex(lineIndex - sb.toString().length())
                  .setEndIndex(lineIndex)
                  .build());
          nextGlobalStartIndex = globalIndex;
        }
      }
      // Add placeholders for REPLACE
      if (change.getType() == ChangeType.REPLACE) {
        int addPlaceholderCount =
            CharMatcher.is('\n').countIn(change.getText())
                - CharMatcher.is('\n').countIn(change.getReplacingText());
        if (sideOperation == ChangeType.DELETE) {
          addPlaceholderCount *= -1;
        }
        addPlaceholderCount = Math.max(addPlaceholderCount, 0);
        for (int i = 0; i < addPlaceholderCount; i++) {
          result.add(
              TextChange.newBuilder()
                  .setType(ChangeType.LINE_PLACEHOLDER)
                  .setLineNumber(lineNumber + i + 1) // A newline is a placeholder on the next line
                  .build());
        }
        lineNumber += addPlaceholderCount;
      }
      sb = new StringBuilder();
    }
    return result.build();
  }

  public TextDiff getTextDiff(String leftContents, String rightContents) {
    if (leftContents.isEmpty() && rightContents.isEmpty()) {
      return TextDiff.getDefaultInstance();
    }
    ImmutableList<DiffPatchMatchChange> changes =
        getDiffPatchMatchChanges(leftContents, rightContents);

    TextDiff diff =
        TextDiff.newBuilder()
            .addAllLeftChange(getSingleSideChanges(changes, ChangeType.DELETE))
            .addAllRightChange(getSingleSideChanges(changes, ChangeType.ADD))
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    return diff;
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

