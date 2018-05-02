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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.TextChange.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link com.google.startupos.common.TextDifferencer}. */
@RunWith(JUnit4.class)
public class TextDifferencerTest {

  private ITextDifferencer differencer;

  /** package */
  TextChange.Builder newTextChange(int leftIndex, int rightIndex, char difference, Type type) {
    return TextChange.newBuilder()
        .setFirstStringIndex(leftIndex)
        .setSecondStringIndex(rightIndex)
        .setDifference(Character.toString(difference))
        .setType(type);
  }

  @Before
  public void setUp() {
    differencer = new TextDifferencer();
  }

  @Test
  public void testEmptyDiff() {

    assertEquals(differencer.getAllTextChanges("", ""), ImmutableList.of());
  }

  @Test
  public void testOnlyAdditions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'A', Type.ADD).build(),
            newTextChange(0, 1, 'd', Type.ADD).build(),
            newTextChange(0, 2, 'd', Type.ADD).build(),
            newTextChange(0, 3, 'i', Type.ADD).build(),
            newTextChange(0, 4, 't', Type.ADD).build(),
            newTextChange(0, 5, 'i', Type.ADD).build(),
            newTextChange(0, 6, 'o', Type.ADD).build(),
            newTextChange(0, 7, 'n', Type.ADD).build(),
            newTextChange(0, 8, '.', Type.ADD).build()),
        differencer.getAllTextChanges("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'D', Type.DELETE).build(),
            newTextChange(1, 0, 'e', Type.DELETE).build(),
            newTextChange(2, 0, 'l', Type.DELETE).build(),
            newTextChange(3, 0, 'e', Type.DELETE).build(),
            newTextChange(4, 0, 't', Type.DELETE).build(),
            newTextChange(5, 0, 'i', Type.DELETE).build(),
            newTextChange(6, 0, 'o', Type.DELETE).build(),
            newTextChange(7, 0, 'n', Type.DELETE).build(),
            newTextChange(8, 0, '.', Type.DELETE).build()),
        differencer.getAllTextChanges("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'N', Type.NO_CHANGE).build(),
            newTextChange(1, 1, 'o', Type.NO_CHANGE).build(),
            newTextChange(2, 2, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 3, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 4, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 5, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 6, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 7, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 8, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, 9, '.', Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("No Change.", "No Change."));
  }

  @Test
  public void testMixedChangesAtTheBeginning() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'N', Type.DELETE).build(),
            newTextChange(1, 0, 'o', Type.DELETE).build(),
            newTextChange(1, 0, 'W', Type.ADD).build(),
            newTextChange(1, 1, 'i', Type.ADD).build(),
            newTextChange(1, 2, 't', Type.ADD).build(),
            newTextChange(1, 3, 'h', Type.ADD).build(),
            newTextChange(2, 4, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 5, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 6, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 7, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 8, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 9, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 10, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, 11, '.', Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("No Change.", "With Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'W', Type.NO_CHANGE).build(),
            newTextChange(1, 1, 'i', Type.NO_CHANGE).build(),
            newTextChange(2, 2, 't', Type.NO_CHANGE).build(),
            newTextChange(3, 3, 'h', Type.NO_CHANGE).build(),
            newTextChange(4, 4, ' ', Type.NO_CHANGE).build(),
            newTextChange(5, 5, 'a', Type.ADD).build(),
            newTextChange(5, 6, ' ', Type.ADD).build(),
            newTextChange(5, 7, 'C', Type.NO_CHANGE).build(),
            newTextChange(6, 8, 'h', Type.NO_CHANGE).build(),
            newTextChange(7, 9, 'a', Type.NO_CHANGE).build(),
            newTextChange(8, 10, 'n', Type.NO_CHANGE).build(),
            newTextChange(9, 11, 'g', Type.NO_CHANGE).build(),
            newTextChange(10, 12, 'e', Type.NO_CHANGE).build(),
            newTextChange(11, 13, '.', Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("With Change.", "With a Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, 'N', Type.NO_CHANGE).build(),
            newTextChange(1, 1, 'o', Type.NO_CHANGE).build(),
            newTextChange(2, 2, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 3, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 4, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 5, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 6, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 7, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 8, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, 9, '.', Type.DELETE).build(),
            newTextChange(9, 9, '!', Type.ADD).build()),
        differencer.getAllTextChanges("No Change.", "No Change!"));
  }
}
