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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link com.google.startup.common.TextDifferencer}. */
@RunWith(JUnit4.class)
public class TextDifferencerTest {

  /** package */
  CharDifference.Builder newCharDifference(
      int leftIndex, int rightIndex, char difference, DifferenceType type) {
    return CharDifference.newBuilder()
        .setFirstStringIndex(leftIndex)
        .setSecondStringIndex(rightIndex)
        .setDifference(Character.toString(difference))
        .setType(type);
  }

  @Test
  public void testEmptyDiff() {
    assertEquals(TextDifferencer.getAllTextDifferences("", ""), ImmutableList.of());
  }

  @Test
  public void testOnlyAdditions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'A', DifferenceType.ADDITION).build(),
            newCharDifference(0, 1, 'd', DifferenceType.ADDITION).build(),
            newCharDifference(0, 2, 'd', DifferenceType.ADDITION).build(),
            newCharDifference(0, 3, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(0, 4, 't', DifferenceType.ADDITION).build(),
            newCharDifference(0, 5, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(0, 6, 'o', DifferenceType.ADDITION).build(),
            newCharDifference(0, 7, 'n', DifferenceType.ADDITION).build(),
            newCharDifference(0, 8, '.', DifferenceType.ADDITION).build()),
        TextDifferencer.getAllTextDifferences("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'D', DifferenceType.DELETION).build(),
            newCharDifference(1, 0, 'e', DifferenceType.DELETION).build(),
            newCharDifference(2, 0, 'l', DifferenceType.DELETION).build(),
            newCharDifference(3, 0, 'e', DifferenceType.DELETION).build(),
            newCharDifference(4, 0, 't', DifferenceType.DELETION).build(),
            newCharDifference(5, 0, 'i', DifferenceType.DELETION).build(),
            newCharDifference(6, 0, 'o', DifferenceType.DELETION).build(),
            newCharDifference(7, 0, 'n', DifferenceType.DELETION).build(),
            newCharDifference(8, 0, '.', DifferenceType.DELETION).build()),
        TextDifferencer.getAllTextDifferences("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'N', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 1, 'o', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, 2, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 3, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 4, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 5, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 6, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 7, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 8, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, 9, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "No Change."));
  }

  @Test
  public void testMixedChangesAtTheBeginning() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'N', DifferenceType.DELETION).build(),
            newCharDifference(1, 0, 'o', DifferenceType.DELETION).build(),
            newCharDifference(1, 0, 'W', DifferenceType.ADDITION).build(),
            newCharDifference(1, 1, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(1, 2, 't', DifferenceType.ADDITION).build(),
            newCharDifference(1, 3, 'h', DifferenceType.ADDITION).build(),
            newCharDifference(2, 4, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 5, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 6, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 7, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 8, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 9, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 10, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, 11, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "With Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'W', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 1, 'i', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, 2, 't', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 3, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 4, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 5, 'a', DifferenceType.ADDITION).build(),
            newCharDifference(5, 6, ' ', DifferenceType.ADDITION).build(),
            newCharDifference(5, 7, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 8, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 9, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 10, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, 11, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(10, 12, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(11, 13, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("With Change.", "With a Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 0, 'N', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 1, 'o', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, 2, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 3, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 4, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 5, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 6, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 7, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 8, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, 9, '.', DifferenceType.DELETION).build(),
            newCharDifference(9, 9, '!', DifferenceType.ADDITION).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "No Change!"));
  }
}
