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

package com.google.startup.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An implementation of text difference based on the Longest Common Subsequence problem (A.K.A LCS).
 */
public class TextDifferencer {

  /**
   * Return all the text differences between two given strings.
   *
   * @param firstString The first string.
   * @param secondString The second string.
   * @return A list which holds all the text differences.
   */
  public static List<CharDifference> getAllTextDifferences(
      String firstString, String secondString) {
    final char[] first = firstString.toCharArray();
    final char[] second = secondString.toCharArray();
    int headerLength = getHeaderMatchingCharactersLength(first, second);
    int footerLength = getFooterMatchingCharactersLength(headerLength, first, second);
    StringView firstView =
        new StringView(first, headerLength, first.length - footerLength - headerLength);
    StringView secondView =
        new StringView(second, headerLength, second.length - footerLength - headerLength);

    List<CharDifference> allDifferences = new ArrayList<>();
    allDifferences.addAll(getMatchingCharDifferences(first, 0, headerLength));
    allDifferences.addAll(
        getDifferencesFromLCSMatrix(
            computeLCSMatrix(firstView, secondView), firstView, secondView));
    allDifferences.addAll(
        getMatchingCharDifferences(first, first.length - footerLength, footerLength));
    return allDifferences;
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
   * Generate matching char differences for the given range. The implementation assumes that all the
   * characters within the given range are equal.
   *
   * @param content The contents of the first string.
   * @param begin The beginning index of the matching character range.
   * @param length the length of the matching character range.
   * @return A {@link Stream} which holds all the text differences.
   */
  private static List<CharDifference> getMatchingCharDifferences(
      char[] content, int begin, int length) {
    return IntStream.range(begin, begin + length)
        .mapToObj(
            (i) ->
                CharDifference.newBuilder()
                    .setIndex(i)
                    .setDifference(Character.toString(content[i]))
                    .setType(DifferenceType.NO_CHANGE)
                    .build())
        .collect(Collectors.toList());
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
  private static int[][] computeLCSMatrix(final StringView first, final StringView second) {
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
   * Compute all the character differences between two strings based on the precomputed Longest
   * Common Subsequence matrix.
   *
   * @param lcsMatrix The precomputed longest subsequence matrix.
   * @param first The first string of two strings to be compared with.
   * @param second The second string of two strings to be compared with.
   * @return A list of all the character differences
   */
  private static List<CharDifference> getDifferencesFromLCSMatrix(
      final int[][] lcsMatrix, final StringView first, final StringView second) {
    List<CharDifference> differences = new ArrayList<>();
    int i = first.length();
    int j = second.length();
    while (i >= 0 || j >= 0) {
      if (i > 0 && j > 0 && first.charAt(i - 1) == second.charAt(j - 1)) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(first.begin() + i - 1)
                .setDifference(Character.toString(first.charAt(i - 1)))
                .setType(DifferenceType.NO_CHANGE)
                .build());
        i--;
        j--;
      } else if (j > 0 && (i == 0 || lcsMatrix[i][j - 1] >= lcsMatrix[i - 1][j])) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(second.begin() + j - 1)
                .setDifference(Character.toString(second.charAt(j - 1)))
                .setType(DifferenceType.ADDITION)
                .build());
        j--;
      } else if (i > 0 && (j == 0 || lcsMatrix[i][j - 1] < lcsMatrix[i - 1][j])) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(first.begin() + i - 1)
                .setDifference(Character.toString(first.charAt(i - 1)))
                .setType(DifferenceType.DELETION)
                .build());
        i--;
      } else {
        break;
      }
    }
    Collections.reverse(differences);
    return differences;
  }

  private TextDifferencer() {}

  /** Holds a partial substring of a string. */
  private static class StringView {

    private final char[] content;
    private int begin;
    private int length;

    StringView(final char[] content, int begin, int length) {
      this.content = content;
      this.begin = begin;
      this.length = length;
    }

    /** Returns the begin index in which the substring starts. */
    int begin() {
      return begin;
    }

    /** Returns the length of the substring. */
    int length() {
      return length;
    }

    char charAt(int i) {
      return this.content[begin() + i];
    }
  }
}
