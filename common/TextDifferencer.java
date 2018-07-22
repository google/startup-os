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
import com.google.startupos.common.TextChange.Type;
import name.fraser.neil.plaintext.DiffMatchPatch;
import name.fraser.neil.plaintext.DiffMatchPatch.Operation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.swing.text.Segment;
import java.util.LinkedList;

/** An implementation of text difference based on the Longest Common Subsequence (LCS) problem. */
public class TextDifferencer {

  @Inject
  public TextDifferencer() {}

  /**
   * Return all the text differences between two strings.
   *
   * @param firstString The first string.
   * @param secondString The second string.
   * @return A list of text changes.
   */
  public ImmutableList<TextChange> getAllTextChanges(String firstString, String secondString) {
    ImmutableList.Builder<TextChange> result = ImmutableList.builder();
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(firstString, secondString);
    diffMatchPatch.diff_cleanupSemantic(diffs);
    int firstIndex = 0;
    int secondIndex = 0;
    for (DiffMatchPatch.Diff diff : diffs) {
      TextChange.Builder textChange =
          TextChange.newBuilder().setText(diff.text).setType(getType(diff.operation));
      if (diff.operation == Operation.EQUAL) {
        textChange.setFirstStringIndex(firstIndex);
        textChange.setSecondStringIndex(secondIndex);
        firstIndex += diff.text.length();
        secondIndex += diff.text.length();
      } else if (diff.operation == Operation.INSERT) {
        textChange.setFirstStringIndex(-1);
        textChange.setSecondStringIndex(secondIndex);
        secondIndex += diff.text.length();
      } else if (diff.operation == Operation.DELETE) {
        textChange.setFirstStringIndex(firstIndex);
        textChange.setSecondStringIndex(-1);
        firstIndex += diff.text.length();
      }
      result.add(textChange.build());
    }
    return result.build();
  }

  private Type getType(Operation operation) {
    if (operation == Operation.EQUAL) {
      return Type.NO_CHANGE;
    } else if (operation == Operation.INSERT) {
      return Type.ADD;
    } else if (operation == Operation.DELETE) {
      return Type.DELETE;
    } else {
      throw new IllegalArgumentException("Unknown Operation enum: " + operation);
    }
  }
}

