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

  private TextDifferencer differencer;

  /** package */
  private TextChange.Builder newTextChange(
      int leftIndex, int rightIndex, String difference, Type type) {
    return TextChange.newBuilder()
        .setFirstStringIndex(leftIndex)
        .setSecondStringIndex(rightIndex)
        .setText(difference)
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
  public void testOnlyAdditions() {
    assertEquals(
        ImmutableList.of(newTextChange(-1, 0, "Addition.", Type.ADD).build()),
        differencer.getAllTextChanges("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() {
    assertEquals(
        ImmutableList.of(newTextChange(0, -1, "Deletion.", Type.DELETE).build()),
        differencer.getAllTextChanges("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() {
    assertEquals(
        ImmutableList.of(newTextChange(0, 0, "No Change.", Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("No Change.", "No Change."));
  }

  @Test
  public void testMixedChangesAtTheBeginning() {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, -1, "No", Type.DELETE).build(),
            newTextChange(-1, 0, "With", Type.ADD).build(),
            newTextChange(2, 4, " Change.", Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("No Change.", "With Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, "With ", Type.NO_CHANGE).build(),
            newTextChange(-1, 5, "a ", Type.ADD).build(),
            newTextChange(5, 7, "Change.", Type.NO_CHANGE).build()),
        differencer.getAllTextChanges("With Change.", "With a Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 0, "No Change", Type.NO_CHANGE).build(),
            newTextChange(9, -1, ".", Type.DELETE).build(),
            newTextChange(-1, 9, "!", Type.ADD).build()),
        differencer.getAllTextChanges("No Change.", "No Change!"));
  }
}

