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
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.TextDiff;
import dagger.Component;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link com.google.startupos.common.TextDifferencer}. */
@RunWith(JUnit4.class)
public class TextDifferencerTest {

  private TextDifferencer differencer;
  private FileUtils fileUtils;

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
    fileUtils = DaggerTextDifferencerTest_TestComponent.create().getFileUtils();
  }

  protected String getGoldenFile(String filename) {
    return fileUtils.readFileFromResourcesUnchecked("common/tests/resources/" + filename);
  }

  @Test
  public void testEmptyDiff() {
    assertEquals(TextDiff.getDefaultInstance(), differencer.getTextDiff("", "", ""));
  }

  @Test
  public void testLeftAndRightAreEqual() {
    String text = "aaa";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .setLeftFileContents(text)
            .setRightFileContents(text)
            .build();
    assertEquals(expectedTextDiff, differencer.getTextDiff(text, text, ""));
  }

  @Test
  public void testOnlyAdditions() {
    String leftContents = "";
    String rightContents = "Addition.";
    String diff = "@@ -0,0 +1 @@\n+Addition.";

    TextDiff expectedTextDiff =
        TextDiff.newBuilder()
            .addRightChange(
                TextChange.newBuilder().setText(rightContents).setType(ChangeType.ADD).build())
            .addLeftChange(TextChange.newBuilder().setType(ChangeType.LINE_PLACEHOLDER).build())
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    assertEquals(
        expectedTextDiff,
        differencer.getTextDiff(leftContents, rightContents, diff));
  }

      @Test
      public void testOnlyDeletions() {
        String leftContents = "Deletion.";
        String rightContents = "";
        String diff = "@@ -1 +0,0 @@\n-Deletion.";

        TextDiff expectedTextDiff =
            TextDiff.newBuilder()
                .addLeftChange(
                    TextChange.newBuilder()
                        .setText(leftContents)
                        .setType(ChangeType.DELETE)
                        .build())
                .addRightChange(
                    TextChange.newBuilder()
                        .setText(rightContents)
                        .setType(ChangeType.LINE_PLACEHOLDER)
                        .build())
                .setLeftFileContents(leftContents)
                .setRightFileContents(rightContents)
                .build();
        assertEquals(expectedTextDiff, differencer.getTextDiff(leftContents, rightContents, diff));
      }

      @Test
      public void testOnlyNoChanges() {
        String contents = "No Change.";

        TextDiff expectedTextDiff =
            TextDiff.newBuilder()
                .setLeftFileContents(contents)
                .setRightFileContents(contents)
                .build();
        assertEquals(expectedTextDiff, differencer.getTextDiff(contents, contents, ""));
      }

      @Test
      public void testMixedChangesAtTheBeginning() {
        String leftContents = "No Change.";
        String rightContents = "With Change.";
        String diff = "@@ -1 +1 @@\n-No Change.\n+With Change.";

        TextDiff expectedTextDiff =
            TextDiff.newBuilder()
                .addLeftChange(
                    TextChange.newBuilder()
                        .setText("No Change.")
                        .setType(ChangeType.DELETE)
                        .build())
                .addRightChange(
                    TextChange.newBuilder()
                        .setText("With Change.")
                        .setType(ChangeType.ADD)
                        .build())
                .setLeftFileContents(leftContents)
                .setRightFileContents(rightContents)
                .build();
        assertEquals(expectedTextDiff, differencer.getTextDiff(leftContents, rightContents, diff));
      }

      @Test
      public void testMixedChangesAtTheMiddle() {
        String leftContents = "With Change.";
        String rightContents = "With a Change.";
        String diff = "@@ -1 +1 @@\n-With Change.\n+With a Change.";

        TextDiff expectedTextDiff =
            TextDiff.newBuilder()
                .addLeftChange(
                    TextChange.newBuilder()
                        .setText("With Change.")
                        .setType(ChangeType.DELETE)
                        .build())
                .addRightChange(
                    TextChange.newBuilder()
                        .setText("With a Change.")
                        .setType(ChangeType.ADD)
                        .build())
                .setLeftFileContents(leftContents)
                .setRightFileContents(rightContents)
                .build();
        assertEquals(expectedTextDiff, differencer.getTextDiff(leftContents, rightContents, diff));
      }

      @Test
      public void testMixedChangesAtTheEnd() {
        String leftContents = "Change at end.";
        String rightContents = "Change at end!";
        String diff = "@@ -1 +1 @@\n-Change at end.\n+Change at end!";

        TextDiff expectedTextDiff =
            TextDiff.newBuilder()
                .addLeftChange(
                    TextChange.newBuilder()
                        .setText("Change at end.")
                        .setType(ChangeType.DELETE)
                        .build())
                .addRightChange(
                    TextChange.newBuilder()
                        .setText("Change at end!")
                        .setType(ChangeType.ADD)
                        .build())
                .setLeftFileContents(leftContents)
                .setRightFileContents(rightContents)
                .build();

        assertEquals(expectedTextDiff, differencer.getTextDiff(leftContents, rightContents, diff));
      }

    @Test
    public void testBuildFileChange() {
      String leftContents = getGoldenFile("BUILD_before.txt");
      String rightContents = getGoldenFile("BUILD_after.txt");
      String diffString = getGoldenFile("BUILD_diff.txt");

      TextDiff expectedTextDiff =
          TextDiff.newBuilder()
              .addLeftChange(
                  TextChange.newBuilder()
                      .setType(ChangeType.LINE_PLACEHOLDER)
                      .setLineNumber(12)
                      .build())
              .addRightChange(
                  TextChange.newBuilder()
                      .setText("        \"//common/repo:repo_java_proto\",")
                      .setType(ChangeType.ADD)
                      .setLineNumber(12)
                      .build())
              .setLeftFileContents(leftContents)
              .setRightFileContents(rightContents)
              .build();

      assertEquals(
          expectedTextDiff, differencer.getTextDiff(leftContents, rightContents, diffString));
    }

  @Singleton
  @Component(modules = CommonModule.class)
  interface TestComponent {
    FileUtils getFileUtils();
  }
}

