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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.TextChange.Type;

/** Tests for {@link com.google.startup.common.TextDifferencer}. */
@RunWith(JUnit4.class)
public class TextDifferencerTest {

  TextChange.Builder newTextChange(int index, char difference, Type type) {
    return TextChange.newBuilder()
        .setIndex(index)
        .setDifference(Character.toString(difference))
        .setType(type);
  }

  @Test
  public void testEmptyDiff() {
    assertEquals(TextDifferencer.getAllTextChanges("", ""), ImmutableList.of());
  }

  @Test
  public void testOnlyAdditions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'A', Type.ADD).build(),
            newTextChange(1, 'd', Type.ADD).build(),
            newTextChange(2, 'd', Type.ADD).build(),
            newTextChange(3, 'i', Type.ADD).build(),
            newTextChange(4, 't', Type.ADD).build(),
            newTextChange(5, 'i', Type.ADD).build(),
            newTextChange(6, 'o', Type.ADD).build(),
            newTextChange(7, 'n', Type.ADD).build(),
            newTextChange(8, '.', Type.ADD).build()),
        TextDifferencer.getAllTextChanges("", "Addition."));
  }

  @Test
  public void testOnlyDeletions() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'D', Type.DELETE).build(),
            newTextChange(1, 'e', Type.DELETE).build(),
            newTextChange(2, 'l', Type.DELETE).build(),
            newTextChange(3, 'e', Type.DELETE).build(),
            newTextChange(4, 't', Type.DELETE).build(),
            newTextChange(5, 'i', Type.DELETE).build(),
            newTextChange(6, 'o', Type.DELETE).build(),
            newTextChange(7, 'n', Type.DELETE).build(),
            newTextChange(8, '.', Type.DELETE).build()),
        TextDifferencer.getAllTextChanges("Deletion.", ""));
  }

  @Test
  public void testOnlyNoChanges() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'N', Type.NO_CHANGE).build(),
            newTextChange(1, 'o', Type.NO_CHANGE).build(),
            newTextChange(2, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, '.', Type.NO_CHANGE).build()),
        TextDifferencer.getAllTextChanges("No Change.", "No Change."));
  }

  @Test
  public void testMixedChangesAtTheBeginning() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'N', Type.DELETE).build(),
            newTextChange(1, 'o', Type.DELETE).build(),
            newTextChange(0, 'W', Type.ADD).build(),
            newTextChange(1, 'i', Type.ADD).build(),
            newTextChange(2, 't', Type.ADD).build(),
            newTextChange(3, 'h', Type.ADD).build(),
            newTextChange(2, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, '.', Type.NO_CHANGE).build()),
        TextDifferencer.getAllTextChanges("No Change.", "With Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'W', Type.NO_CHANGE).build(),
            newTextChange(1, 'i', Type.NO_CHANGE).build(),
            newTextChange(2, 't', Type.NO_CHANGE).build(),
            newTextChange(3, 'h', Type.NO_CHANGE).build(),
            newTextChange(4, ' ', Type.NO_CHANGE).build(),
            newTextChange(5, 'a', Type.ADD).build(),
            newTextChange(6, ' ', Type.ADD).build(),
            newTextChange(5, 'C', Type.NO_CHANGE).build(),
            newTextChange(6, 'h', Type.NO_CHANGE).build(),
            newTextChange(7, 'a', Type.NO_CHANGE).build(),
            newTextChange(8, 'n', Type.NO_CHANGE).build(),
            newTextChange(9, 'g', Type.NO_CHANGE).build(),
            newTextChange(10, 'e', Type.NO_CHANGE).build(),
            newTextChange(11, '.', Type.NO_CHANGE).build()),
        TextDifferencer.getAllTextChanges("With Change.", "With a Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() throws Exception {
    assertEquals(
        ImmutableList.of(
            newTextChange(0, 'N', Type.NO_CHANGE).build(),
            newTextChange(1, 'o', Type.NO_CHANGE).build(),
            newTextChange(2, ' ', Type.NO_CHANGE).build(),
            newTextChange(3, 'C', Type.NO_CHANGE).build(),
            newTextChange(4, 'h', Type.NO_CHANGE).build(),
            newTextChange(5, 'a', Type.NO_CHANGE).build(),
            newTextChange(6, 'n', Type.NO_CHANGE).build(),
            newTextChange(7, 'g', Type.NO_CHANGE).build(),
            newTextChange(8, 'e', Type.NO_CHANGE).build(),
            newTextChange(9, '.', Type.DELETE).build(),
            newTextChange(9, '!', Type.ADD).build()),
        TextDifferencer.getAllTextChanges("No Change.", "No Change!"));
  }
}
