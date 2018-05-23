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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.swing.text.Segment;

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
    final char[] first = firstString.toCharArray();
    final char[] second = secondString.toCharArray();
    int headerLength = getHeaderMatchingCharactersLength(first, second);
    int footerLength = getFooterMatchingCharactersLength(headerLength, first, second);
    Segment firstSegment =
        new Segment(first, headerLength, first.length - footerLength - headerLength);
    Segment secondSegment =
        new Segment(second, headerLength, second.length - footerLength - headerLength);

    List<TextChange> allChanges = new ArrayList<>();
    allChanges.addAll(getMatchingTextChanges(first, 0, 0, headerLength));
    allChanges.addAll(getNonMatchingTextChanges(firstSegment, secondSegment));
    allChanges.addAll(
        getMatchingTextChanges(
            first, first.length - footerLength, second.length - footerLength, footerLength));
    return unifyTextChanges(allChanges);
  }

  /** Unifies TextChanges from chars into strings  */
  private static ImmutableList<TextChange> unifyTextChanges(List<TextChange> changes) {
    ImmutableList.Builder<TextChange> result = new ImmutableList.Builder<>();
    TextChange.Builder unified = null;
    TextChange previous = null;
    for (TextChange current : changes) {
      if (previous == null) {
        previous = current;
        unified = current.toBuilder();
        continue;
      }
      if (previous.getType() == current.getType()) {
        unified = unified.setDifference(unified.getDifference() + current.getDifference());
      } else {
        result.add(unified.build());
        unified = current.toBuilder();
      }
      previous = current;
    }
    if (unified != null && (previous.getType() == unified.getType())) {
      // Should add last part
      result.add(unified.build());
    }
    return result.build();
  }

  /** Count the number of equal characters from the beginning of the given two strings. */
  private static int getHeaderMatchingCharactersLength(final char[] first, final char[] second) {
    int count = 0;
    for (; count < first.length && count < second.length; count++) {
      if (first[count] != second[count]) {
        return count;
      }
    }
    return count;
  }

  /** Count the number of equal characters from the end of the given two strings. */
  private static int getFooterMatchingCharactersLength(
      int offset, final char[] first, final char[] second) {
    int count = 0;
    for (; count < first.length - offset && count < second.length - offset; count++) {
      if (first[first.length - count - 1] != second[second.length - count - 1]) {
        return count;
      }
    }
    return count;
  }

  /**
   * Generate matching text changes for the given range. The implementation assumes that all the
   * characters within the given range are equal.
   *
   * @param contentFirst The contents of the first string.
   * @param beginFirst The beginning index of the matching character range of the first string.
   * @param beginSecond The beginning index of the matching character range of the second string.
   * @param length the length of the matching character range.
   * @return A {@link List} which holds all the text differences.
   */
  private static List<TextChange> getMatchingTextChanges(
      char[] content, int beginFirst, int beginSecond, int length) {
    return IntStream.range(0, length)
        .mapToObj(
            (i) ->
                TextChange.newBuilder()
                    .setFirstStringIndex(i + beginFirst)
                    .setSecondStringIndex(i + beginSecond)
                    .setDifference(Character.toString(content[i + beginFirst]))
                    .setType(Type.NO_CHANGE)
                    .build())
        .collect(Collectors.toList());
  }
  /**
   * Generate non matching text changes for the given range. Non matching text changes are changes
   * which contains at least one change between the given strings.
   *
   * @param contentFirst The contents of the first string.
   * @param beginFirst The beginning index of the matching character range of the first string.
   * @param beginSecond The beginning index of the matching character range of the second string.
   * @param length the length of the matching character range.
   * @return A {@link List} which holds all the text differences.
   */
  private List<TextChange> getNonMatchingTextChanges(Segment firstSegment, Segment secondSegment) {
    return getChangesFromLCSMatrix(
        computeLCSMatrix(firstSegment, secondSegment), firstSegment, secondSegment);
  }

  /** Create an empty matrix based on the given dimensions. */
  private static int[][] createEmptyLCSMatrix(int rowSize, int colSize) {
    final int[][] lcsMatrix = new int[rowSize][];
    for (int i = 0; i < lcsMatrix.length; i++) {
      lcsMatrix[i] = new int[colSize];
    }
    return lcsMatrix;
  }

  /**
   * Precompute all the Longest Subsequence Matrix which holds the length of each common
   * subsequence.
   *
   * @param first The first string out of two strings for precomputing a common subsequence.
   * @param second The second string out of two strings for precomputing a common subsequence.
   * @return A length of the common subsequence for the two strings.
   */
  private static int[][] computeLCSMatrix(final Segment first, final Segment second) {
    final int[][] lcsMatrix = createEmptyLCSMatrix(first.length() + 1, second.length() + 1);
    for (int i = 1; i < first.length() + 1; i++) {
      for (int j = 1; j < second.length() + 1; j++) {
        if (first.charAt(i - 1) == second.charAt(j - 1)) {
          lcsMatrix[i][j] = lcsMatrix[i - 1][j - 1] + 1;
        } else {
          lcsMatrix[i][j] = Math.max(lcsMatrix[i][j - 1], lcsMatrix[i - 1][j]);
        }
      }
    }
    return lcsMatrix;
  }

  /**
   * Compute all the text changes between two strings based on the precomputed Longest Common
   * Subsequence matrix.
   *
   * @param lcsMatrix The precomputed longest subsequence matrix.
   * @param first The first string
   * @param second The second string
   * @return A list of text changes
   */
  private static List<TextChange> getChangesFromLCSMatrix(
      final int[][] lcsMatrix, final Segment first, final Segment second) {
    List<TextChange> changes = new ArrayList<>();
    int i = first.length();
    int j = second.length();
    while (i >= 0 || j >= 0) {
      if (i > 0 && j > 0 && first.charAt(i - 1) == second.charAt(j - 1)) {
        changes.add(
            TextChange.newBuilder()
                .setFirstStringIndex(first.getBeginIndex() + i - 1)
                .setSecondStringIndex(second.getBeginIndex() + j - 1)
                .setDifference(Character.toString(first.charAt(i - 1)))
                .setType(Type.NO_CHANGE)
                .build());
        i--;
        j--;
      } else if (j > 0 && (i == 0 || lcsMatrix[i][j - 1] >= lcsMatrix[i - 1][j])) {
        changes.add(
            TextChange.newBuilder()
                .setFirstStringIndex(first.getBeginIndex() + Math.max(0, i - 1))
                .setSecondStringIndex(second.getBeginIndex() + j - 1)
                .setDifference(Character.toString(second.charAt(j - 1)))
                .setType(Type.ADD)
                .build());
        j--;
      } else if (i > 0 && (j == 0 || lcsMatrix[i][j - 1] < lcsMatrix[i - 1][j])) {
        changes.add(
            TextChange.newBuilder()
                .setFirstStringIndex(first.getBeginIndex() + i - 1)
                .setSecondStringIndex(second.getBeginIndex() + Math.max(0, j - 1))
                .setDifference(Character.toString(first.charAt(i - 1)))
                .setType(Type.DELETE)
                .build());
        i--;
      } else {
        break;
      }
    }
    Collections.reverse(changes);
    return changes;
  }
}

