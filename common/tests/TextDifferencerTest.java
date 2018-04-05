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

  @Test
  public void testEmptyDiff() {
    assertEquals(TextDifferencer.getAllTextDifferences("", ""), ImmutableList.of());
  }

  @Test
  public void testOnlyAdditions() throws Exception {
    assertEquals(
        ImmutableList.of(
            new CharDifference(0, 'A', DifferenceType.ADDITION),
            new CharDifference(1, 'd', DifferenceType.ADDITION),
            new CharDifference(2, 'd', DifferenceType.ADDITION),
            new CharDifference(3, 'i', DifferenceType.ADDITION),
            new CharDifference(4, 't', DifferenceType.ADDITION),
            new CharDifference(5, 'i', DifferenceType.ADDITION),
            new CharDifference(6, 'o', DifferenceType.ADDITION),
            new CharDifference(7, 'n', DifferenceType.ADDITION),
            new CharDifference(8, '.', DifferenceType.ADDITION)),
        TextDifferencer.getAllTextDifferences("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() throws Exception {
    assertEquals(
        ImmutableList.of(
            new CharDifference(0, 'D', DifferenceType.DELETION),
            new CharDifference(1, 'e', DifferenceType.DELETION),
            new CharDifference(2, 'l', DifferenceType.DELETION),
            new CharDifference(3, 'e', DifferenceType.DELETION),
            new CharDifference(4, 't', DifferenceType.DELETION),
            new CharDifference(5, 'i', DifferenceType.DELETION),
            new CharDifference(6, 'o', DifferenceType.DELETION),
            new CharDifference(7, 'n', DifferenceType.DELETION),
            new CharDifference(8, '.', DifferenceType.DELETION)),
        TextDifferencer.getAllTextDifferences("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            new CharDifference(0, 'N', DifferenceType.NO_CHANGE),
            new CharDifference(1, 'o', DifferenceType.NO_CHANGE),
            new CharDifference(2, ' ', DifferenceType.NO_CHANGE),
            new CharDifference(3, 'C', DifferenceType.NO_CHANGE),
            new CharDifference(4, 'h', DifferenceType.NO_CHANGE),
            new CharDifference(5, 'a', DifferenceType.NO_CHANGE),
            new CharDifference(6, 'n', DifferenceType.NO_CHANGE),
            new CharDifference(7, 'g', DifferenceType.NO_CHANGE),
            new CharDifference(8, 'e', DifferenceType.NO_CHANGE),
            new CharDifference(9, '.', DifferenceType.NO_CHANGE)),
        TextDifferencer.getAllTextDifferences("No Change.", "No Change."));
  }

  @Test
  public void testMixedChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            new CharDifference(0, 'N', DifferenceType.DELETION),
            new CharDifference(1, 'o', DifferenceType.DELETION),
            new CharDifference(0, 'W', DifferenceType.ADDITION),
            new CharDifference(1, 'i', DifferenceType.ADDITION),
            new CharDifference(2, 't', DifferenceType.ADDITION),
            new CharDifference(3, 'h', DifferenceType.ADDITION),
            new CharDifference(2, ' ', DifferenceType.NO_CHANGE),
            new CharDifference(3, 'C', DifferenceType.NO_CHANGE),
            new CharDifference(4, 'h', DifferenceType.NO_CHANGE),
            new CharDifference(5, 'a', DifferenceType.NO_CHANGE),
            new CharDifference(6, 'n', DifferenceType.NO_CHANGE),
            new CharDifference(7, 'g', DifferenceType.NO_CHANGE),
            new CharDifference(8, 'e', DifferenceType.NO_CHANGE),
            new CharDifference(9, '.', DifferenceType.NO_CHANGE)),
        TextDifferencer.getAllTextDifferences("No Change.", "With Change."));
  }
}
