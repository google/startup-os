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
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.TextDiff;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link com.google.startupos.common.TextDifferencer}. */
@RunWith(JUnit4.class)
public class TextDifferencerTest {

  private TextDifferencer differencer;

  private TextChange textChange(
      String text, ChangeType type, int lineNumber, int startIndex, int endIndex) {
    return TextChange.newBuilder()
        .setText(text)
        .setType(type)
        .setLineNumber(lineNumber)
        .setStartIndex(startIndex)
        .setEndIndex(endIndex)
        .build();
  }

  @Before
  public void setUp() {
    differencer = new TextDifferencer();
  }

  @Test
  public void testEmptyDiff2() {
    assertEquals(TextDiff.getDefaultInstance(), differencer.getTextDiff2("", "", ""));
  }

  @Test
  public void testLeftAndRightAreEqual() {
    String text = "aaa";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .setLeftFileContents(text)
            .setRightFileContents(text)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff2(text, text, text));
  }

  @Test
  public void testOnlyAdditions2() {
    String leftContents = "";
    String rightContents = "Addition.";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addRightChange(
                TextChange.newBuilder()
                    .setText(rightContents)
                    .setType(ChangeType.ADD)
                    .setEndIndex(9)
                    .build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff2(leftContents, rightContents, "{+Addition.+}"));
  }

  @Test
  public void testOnlyDeletions2() {
    String leftContents = "Deletion.";
    String rightContents = "";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addLeftChange(
                TextChange.newBuilder()
                    .setText(leftContents)
                    .setType(ChangeType.DELETE)
                    .setEndIndex(9)
                    .build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff2(leftContents, rightContents, "[-Deletion.-]"));
  }

  @Test
  public void testOnlyNoChanges2() {
    String contents = "No Change.";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .setLeftFileContents(contents)
            .setRightFileContents(contents)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff2(contents, contents, ""));
  }

  @Test
  public void testMixedChangesAtTheBeginning() {
    String leftContents = "No Change.";
    String rightContents = "With Change.";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addLeftChange(
                TextChange.newBuilder()
                    .setText("No")
                    .setType(ChangeType.DELETE)
                    .setEndIndex(2)
                    .build())
            .addRightChange(
                TextChange.newBuilder()
                    .setText("With")
                    .setType(ChangeType.ADD)
                    .setEndIndex(4)
                    .build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff2(leftContents, rightContents, "[-No-]{+With+} Change."));
  }

  @Test
  public void testMixedChangesAtTheMiddle() {
    String leftContents = "With Change.";
    String rightContents = "With a Change.";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addRightChange(
                TextChange.newBuilder()
                    .setText("a ")
                    .setType(ChangeType.ADD)
                    .setStartIndex(5)
                    .setEndIndex(7)
                    .build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();

    assertEquals(expectedTextDiff, differencer.getTextDiff2(leftContents, rightContents, "With {+a +}Change."));
  }

  @Test
  public void testMixedChangesAtTheEnd() {
    String leftContents = "Change at end.";
    String rightContents = "Change at end!";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addLeftChange(
                TextChange.newBuilder()
                    .setText(".")
                    .setType(ChangeType.DELETE)
                    .setStartIndex(13)
                    .setEndIndex(14)
                    .build())
            .addRightChange(
                TextChange.newBuilder()
                    .setText("!")
                    .setType(ChangeType.ADD)
                    .setStartIndex(13)
                    .setEndIndex(14)
                    .build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();

    assertEquals(expectedTextDiff, differencer.getTextDiff2(leftContents, rightContents, "Change at end[-.-]{+!+}"));
  }
}

