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

package com.google.startupos.tools.reviewer.job.sync.tests;

import com.google.startupos.tools.reviewer.job.sync.LineNumberConverter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineNumberConverterTest {

  private static final String COMPLEX_CHANGES_PATCH =
      "@@ -2,12 +2,6 @@"
          + " line1\n line2\n line3\n line4"
          + "\n-line5\n-line6\n-line7\n-line8\n-line9\n-line10"
          + "\n line11\n line12\n line13\n"
          + "@@ -19,11 +13,11 @@"
          + " line18\n line19\n line20\n line21"
          + "\n-line22\n-line23\n-line24\n-line25\n-line26"
          + "\n+CHANGED_line22\n+CHANGED_line23\n+CHANGED_line24\n+CHANGED_line25\n+CHANGED_line26"
          + "\n line27\n line28\n line29\n"
          + "@@ -32,12 +26,19 @@"
          + " line31\n line32\n line33\n line34"
          + "\n+ADDED_LINE_#1\n+ADDED_LINE_#2\n+ADDED_LINE_#3\n+ADDED_LINE_#4"
          + "\n line35\n line36\n line37\n line38\n line39\n line40"
          + "\n+\n+\n+"
          + "\n line41\n line42\n line43\n"
          + "@@ -62,7 +63,7 @@"
          + " line61\n line62\n line63\n line64"
          + "\n-line65"
          + "\n+GHANGED_line65"
          + "\n line66\n line67\n line68\n"
          + "@@ -84,17 +85,22 @@"
          + " line83\n line84\n line85\n line86"
          + "\n-line87\n-line88\n line89"
          + "\n line90\n line91\n line92\n line93"
          + "\n+\n+"
          + "\n line94\n line95\n line96"
          + "\n-line97\n-line98\n-line99"
          + "\n+GHANGED_line97\n+GHANGED_line98\n+GHANGED_line99"
          + "\n line100"
          + "\n+ADDED_LINE_#5\n+ADDED_LINE_#6\n+ADDED_LINE_#7\n+ADDED_LINE_#8\n+";

  private LineNumberConverter converter = new LineNumberConverter();

  @Test
  public void addedFile_firstLineTest() {
    String patch = "@@ -0,0 +1,5 @@" + "\n+line 1\n+line 2\n+line 3\n+line 4\n+line 5";

    assertEquals(1, converter.getLineNumber(patch, 1, LineNumberConverter.Side.RIGHT));
    assertEquals(1, converter.getPosition(patch, 1, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void addedFile_lineInTheMiddleTest() {
    String patch = "@@ -0,0 +1,5 @@" + "\n+line 1\n+line 2\n+line 3\n+line 4\n+line 5";

    assertEquals(3, converter.getLineNumber(patch, 3, LineNumberConverter.Side.RIGHT));
    assertEquals(3, converter.getPosition(patch, 3, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void addedFile_lastLineTest() {
    String patch = "@@ -0,0 +1,5 @@" + "\n+line 1\n+line 2\n+line 3\n+line 4\n+line 5";

    assertEquals(5, converter.getLineNumber(patch, 5, LineNumberConverter.Side.RIGHT));
    assertEquals(5, converter.getPosition(patch, 5, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void removedFile_firstLineTest() {
    String patch = "@@ -1,5 +0,0 @@" + "\n-line 1\n-line 2\n-line 3\n-line 4\n-line 5";

    assertEquals(1, converter.getLineNumber(patch, 1, LineNumberConverter.Side.LEFT));
    assertEquals(1, converter.getPosition(patch, 1, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void removedFile_lineInTheMiddleTest() {
    String patch = "@@ -1,5 +0,0 @@" + "\n-line 1\n-line 2\n-line 3\n-line 4\n-line 5";

    assertEquals(3, converter.getLineNumber(patch, 3, LineNumberConverter.Side.LEFT));
    assertEquals(3, converter.getPosition(patch, 3, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void removedFile_lastLineTest() {
    String patch = "@@ -1,5 +0,0 @@" + "\n-line 1\n-line 2\n-line 3\n-line 4\n-line 5";

    assertEquals(5, converter.getLineNumber(patch, 5, LineNumberConverter.Side.LEFT));
    assertEquals(5, converter.getPosition(patch, 5, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedOneLine_firstWhiteLineTest() {
    String patch =
        "@@ -2,7 +2,7 @@"
            + " line1\n line2\n line3\n line4"
            + "\n-line5"
            + "\n+CHANGED_line5"
            + "\n line6\n line7\n line8";

    assertEquals(2, converter.getLineNumber(patch, 1, LineNumberConverter.Side.LEFT));
    assertEquals(1, converter.getPosition(patch, 2, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedOneLine_lastWhiteLineTest() {
    String patch =
        "@@ -2,7 +2,7 @@"
            + " line1\n line2\n line3\n line4"
            + "\n-line5"
            + "\n+CHANGED_line5"
            + "\n line6\n line7\n line8";

    assertEquals(8, converter.getLineNumber(patch, 8, LineNumberConverter.Side.LEFT));
    assertEquals(8, converter.getPosition(patch, 8, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedOneLine_changedLineOnTheLeftTest() {
    String patch =
        "@@ -2,7 +2,7 @@"
            + " line1\n line2\n line3\n line4"
            + "\n-line5"
            + "\n+CHANGED_line5"
            + "\n line6\n line7\n line8";

    assertEquals(5, converter.getLineNumber(patch, 4, LineNumberConverter.Side.LEFT));
    assertEquals(4, converter.getPosition(patch, 5, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedOneLine_changedLineOnTheRightTest() {
    String patch =
        "@@ -2,7 +2,7 @@"
            + " line1\n line2\n line3\n line4"
            + "\n-line5"
            + "\n+CHANGED_line5"
            + "\n line6\n line7\n line8";

    assertEquals(5, converter.getLineNumber(patch, 5, LineNumberConverter.Side.RIGHT));
    assertEquals(5, converter.getPosition(patch, 5, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void addedLines_addedLineTest() {
    String patch =
        "@@ -3,6 +3,9 @@"
            + " line2\n line3\n line4\n line5"
            + "\n+NEW_LINE_#1\n+NEW_LINE_#2\n+NEW_LINE_#3"
            + "\n line6\n line7\n line8";

    assertEquals(7, converter.getLineNumber(patch, 5, LineNumberConverter.Side.RIGHT));
    assertEquals(5, converter.getPosition(patch, 7, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void changedThreeLines_firstWhiteLineAfterChangesTest() {
    String patch =
        "@@ -1,9 +1,9 @@"
            + "\n line1\n line2\n line3"
            + "\n-line4\n-line5\n-line6"
            + "\n+CHANGED_line4\n+CHANGED_line5\n+CHANGED_line6"
            + "\n line7\n line8\n line9";

    assertEquals(7, converter.getLineNumber(patch, 10, LineNumberConverter.Side.LEFT));
    assertEquals(10, converter.getPosition(patch, 7, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedThreeLines_lastChangedLineOnTheLeftTest() {
    String patch =
        "@@ -1,9 +1,9 @@"
            + "\n line1\n line2\n line3"
            + "\n-line4\n-line5\n-line6"
            + "\n+CHANGED_line4\n+CHANGED_line5\n+CHANGED_line6"
            + "\n line7\n line8\n line9";

    assertEquals(6, converter.getLineNumber(patch, 6, LineNumberConverter.Side.LEFT));
    assertEquals(6, converter.getPosition(patch, 6, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void changedThreeLines_lastChangedLineOnTheRightTest() {
    String patch =
        "@@ -1,9 +1,9 @@"
            + "\n line1\n line2\n line3"
            + "\n-line4\n-line5\n-line6"
            + "\n+CHANGED_line4\n+CHANGED_line5\n+CHANGED_line6"
            + "\n line7\n line8\n line9";

    assertEquals(6, converter.getLineNumber(patch, 9, LineNumberConverter.Side.RIGHT));
    assertEquals(9, converter.getPosition(patch, 6, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void complexChanges_line24_OnTheLeftTest() {
    assertEquals(
        24, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 19, LineNumberConverter.Side.LEFT));
    assertEquals(
        19, converter.getPosition(COMPLEX_CHANGES_PATCH, 24, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void complexChanges_line38_OnTheLeftTest() {
    assertEquals(
        38, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 41, LineNumberConverter.Side.LEFT));
    assertEquals(
        41, converter.getPosition(COMPLEX_CHANGES_PATCH, 38, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void complexChanges_line65_OnTheLeftTest() {
    assertEquals(
        65, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 54, LineNumberConverter.Side.LEFT));
    assertEquals(
        54, converter.getPosition(COMPLEX_CHANGES_PATCH, 65, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void complexChanges_line100_OnTheLeftTest() {
    assertEquals(
        100, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 81, LineNumberConverter.Side.LEFT));
    assertEquals(
        81, converter.getPosition(COMPLEX_CHANGES_PATCH, 100, LineNumberConverter.Side.LEFT));
  }

  @Test
  public void complexChanges_line18_OnTheRightTest() {
    assertEquals(
        18, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 24, LineNumberConverter.Side.RIGHT));
    assertEquals(
        24, converter.getPosition(COMPLEX_CHANGES_PATCH, 18, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void complexChanges_line30_OnTheRightTest() {
    assertEquals(
        30, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 35, LineNumberConverter.Side.RIGHT));
    assertEquals(
        35, converter.getPosition(COMPLEX_CHANGES_PATCH, 30, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void complexChanges_line100_OnTheRightTest() {
    assertEquals(
        100, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 80, LineNumberConverter.Side.RIGHT));
    assertEquals(
        80, converter.getPosition(COMPLEX_CHANGES_PATCH, 100, LineNumberConverter.Side.RIGHT));
  }

  @Test
  public void complexChanges_line106_OnTheRightTest() {
    assertEquals(
        106, converter.getLineNumber(COMPLEX_CHANGES_PATCH, 86, LineNumberConverter.Side.RIGHT));
    assertEquals(
        86, converter.getPosition(COMPLEX_CHANGES_PATCH, 106, LineNumberConverter.Side.RIGHT));
  }
}

