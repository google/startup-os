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

/**
 * An implementation of text difference based on the Longest Common Subsequence problem (A.K.A LCS).
 */
public class TextDifferencer {

  /**
   * Returns all the text differences between two given strings.
   *
   * @param first The first string.
   * @param second The second string.
   * @return A list which holds all the text differences.
   */
  public static List<CharDifference> getAllTextDifferences(String first, String second) {
    return getDifferencesFromLCSMatrix(
            computeLCSMatrix(first.toCharArray(), second.toCharArray()),
            first.toCharArray(),
            second.toCharArray())
        .stream()
        .map((builder) -> builder.build())
        .collect(Collectors.toList());
  }

  /** Create an empty matrix based on the given dimensions. */
  private static int[][] createEmptyLCSMatrix(int rowSize, int colSize) {
    final int[][] lcsMatrix = new int[rowSize][];
    for (int i = 0; i < lcsMatrix.length; ++i) {
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
  private static int[][] computeLCSMatrix(char[] first, char[] second) {
    final int[][] lcsMatrix = createEmptyLCSMatrix(first.length + 1, second.length + 1);
    for (int i = 1; i < first.length + 1; ++i) {
      for (int j = 1; j < second.length + 1; ++j) {
        if (first[i - 1] == second[j - 1]) {
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
  private static List<CharDifference.Builder> getDifferencesFromLCSMatrix(
      int[][] lcsMatrix, char[] first, char[] second) {
    List<CharDifference.Builder> differences = new ArrayList<>();
    int i = lcsMatrix.length - 1;
    int j = lcsMatrix[0].length - 1;
    while (i >= 0 || j >= 0) {
      if (i > 0 && j > 0 && first[i - 1] == second[j - 1]) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(i - 1)
                .setDifference(Character.toString(first[i - 1]))
                .setType(DifferenceType.NO_CHANGE));
        i--;
        j--;
      } else if (j > 0 && (i == 0 || lcsMatrix[i][j - 1] >= lcsMatrix[i - 1][j])) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(j - 1)
                .setDifference(Character.toString(second[j - 1]))
                .setType(DifferenceType.ADDITION));
        j--;
      } else if (i > 0 && (j == 0 || lcsMatrix[i][j - 1] < lcsMatrix[i - 1][j])) {
        differences.add(
            CharDifference.newBuilder()
                .setIndex(i - 1)
                .setDifference(Character.toString(first[i - 1]))
                .setType(DifferenceType.DELETION));
        i--;
      } else {
        break;
      }
    }
    Collections.reverse(differences);
    return differences;
  }

  private TextDifferencer() {}
}
