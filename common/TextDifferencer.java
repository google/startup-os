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

  private ImmutableList<TextChange> getSingleSideChanges(
      LinkedList<DiffMatchPatch.Diff> diffs, Operation sideOperation) {
    if (diffs.isEmpty()) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<TextChange> result = ImmutableList.builder();
    StringBuilder sb = new StringBuilder();
    int lineNumber = 0;
    int globalIndex = 0;
    int lineIndex = 0;
    int nextGlobalStartIndex = 0;

    for (DiffMatchPatch.Diff diff : diffs) {
      Operation operation = diff.operation;
      for (int i = 0; i < diff.text.length(); i++) {
        if (diff.text.charAt(i) == '\n') {
          if (operation == Operation.EQUAL || operation == sideOperation) {
            result.add(
                TextChange.newBuilder()
                    .setText(sb.toString())
                    .setType(getChangeType(operation))
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
          if (operation == Operation.EQUAL || operation == sideOperation) {
            lineIndex++;
          }
          sb.append(diff.text.charAt(i));
        }
        if (operation == Operation.EQUAL || operation == sideOperation) {
          globalIndex++;
        }
      }
      // Check for leftover from last line
      if (sb.length() > 0) {
        if (operation == Operation.EQUAL || operation == sideOperation) {
          result.add(
              TextChange.newBuilder()
                  .setText(sb.toString())
                  .setType(getChangeType(diff.operation))
                  .setLineNumber(lineNumber)
                  .setGlobalStartIndex(nextGlobalStartIndex)
                  .setGlobalEndIndex(globalIndex)
                  .setStartIndex(lineIndex - sb.toString().length())
                  .setEndIndex(lineIndex)
                  .build());
          nextGlobalStartIndex = globalIndex;
        }
      }

      sb = new StringBuilder();
    }
    // A last newline is added separately since no subsequent character will add a TextDiff for it.
    if (diffs.get(diffs.size() - 1).text.endsWith("\n")) {
      result.add(
          TextChange.newBuilder()
              .setType(ChangeType.LINE_PLACEHOLDER)
              .setLineNumber(lineNumber)
              .build());
    }
    if (result.build().isEmpty()) {
      result.add(TextChange.newBuilder().setType(ChangeType.LINE_PLACEHOLDER).build());
    }
    return result.build();
  }

  public TextDiff getTextDiff(String leftContents, String rightContents) {
    if (leftContents.isEmpty() && rightContents.isEmpty()) {
      return TextDiff.getDefaultInstance();
    }
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(leftContents, rightContents);

    return TextDiff.newBuilder()
        .addAllLeftChange(getSingleSideChanges(diffs, Operation.DELETE))
        .addAllRightChange(getSingleSideChanges(diffs, Operation.INSERT))
        .setLeftFileContents(leftContents)
        .setRightFileContents(rightContents)
        .build();
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

