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

  CharDifference.Builder newCharDifference(int index, char difference, DifferenceType type) {
    return CharDifference.newBuilder()
        .setIndex(index)
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
            newCharDifference(0, 'A', DifferenceType.ADDITION).build(),
            newCharDifference(1, 'd', DifferenceType.ADDITION).build(),
            newCharDifference(2, 'd', DifferenceType.ADDITION).build(),
            newCharDifference(3, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(4, 't', DifferenceType.ADDITION).build(),
            newCharDifference(5, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(6, 'o', DifferenceType.ADDITION).build(),
            newCharDifference(7, 'n', DifferenceType.ADDITION).build(),
            newCharDifference(8, '.', DifferenceType.ADDITION).build()),
        TextDifferencer.getAllTextDifferences("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 'D', DifferenceType.DELETION).build(),
            newCharDifference(1, 'e', DifferenceType.DELETION).build(),
            newCharDifference(2, 'l', DifferenceType.DELETION).build(),
            newCharDifference(3, 'e', DifferenceType.DELETION).build(),
            newCharDifference(4, 't', DifferenceType.DELETION).build(),
            newCharDifference(5, 'i', DifferenceType.DELETION).build(),
            newCharDifference(6, 'o', DifferenceType.DELETION).build(),
            newCharDifference(7, 'n', DifferenceType.DELETION).build(),
            newCharDifference(8, '.', DifferenceType.DELETION).build()),
        TextDifferencer.getAllTextDifferences("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 'N', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 'o', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "No Change."));
  }

  @Test
  public void testMixedChangesAtTheBeginning() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 'N', DifferenceType.DELETION).build(),
            newCharDifference(1, 'o', DifferenceType.DELETION).build(),
            newCharDifference(0, 'W', DifferenceType.ADDITION).build(),
            newCharDifference(1, 'i', DifferenceType.ADDITION).build(),
            newCharDifference(2, 't', DifferenceType.ADDITION).build(),
            newCharDifference(3, 'h', DifferenceType.ADDITION).build(),
            newCharDifference(2, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "With Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 'W', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 'i', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, 't', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 'a', DifferenceType.ADDITION).build(),
            newCharDifference(6, ' ', DifferenceType.ADDITION).build(),
            newCharDifference(5, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(10, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(11, '.', DifferenceType.NO_CHANGE).build()),
        TextDifferencer.getAllTextDifferences("With Change.", "With a Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() throws Exception {
    assertEquals(
        ImmutableList.of(
            newCharDifference(0, 'N', DifferenceType.NO_CHANGE).build(),
            newCharDifference(1, 'o', DifferenceType.NO_CHANGE).build(),
            newCharDifference(2, ' ', DifferenceType.NO_CHANGE).build(),
            newCharDifference(3, 'C', DifferenceType.NO_CHANGE).build(),
            newCharDifference(4, 'h', DifferenceType.NO_CHANGE).build(),
            newCharDifference(5, 'a', DifferenceType.NO_CHANGE).build(),
            newCharDifference(6, 'n', DifferenceType.NO_CHANGE).build(),
            newCharDifference(7, 'g', DifferenceType.NO_CHANGE).build(),
            newCharDifference(8, 'e', DifferenceType.NO_CHANGE).build(),
            newCharDifference(9, '.', DifferenceType.DELETION).build(),
            newCharDifference(9, '!', DifferenceType.ADDITION).build()),
        TextDifferencer.getAllTextDifferences("No Change.", "No Change!"));
  }
}
