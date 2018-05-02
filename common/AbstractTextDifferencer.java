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

import com.google.startupos.common.TextChange.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.text.Segment;

/** An abstract implementation of the {@link TextDifferencer}. */
public abstract class AbstractTextDifferencer implements TextDifferencer {

  public AbstractTextDifferencer() {}

  @Override
  public List<TextChange> getAllTextChanges(String firstString, String secondString) {
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
    return allChanges;
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
  protected abstract List<TextChange> getNonMatchingTextChanges(
      Segment firstSegment, Segment secondSegment);

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
}
