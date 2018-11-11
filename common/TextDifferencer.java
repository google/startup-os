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

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.startupos.common.Protos.ChangeType;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch;
import com.google.startupos.name.fraser.neil.plaintext.DiffMatchPatch.Operation;
import com.google.startupos.common.Protos.TextDiff;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.DiffPatchMatchChange;
import com.google.startupos.common.Protos.DiffLine;
import com.google.startupos.common.Protos.DiffTag;
import com.google.startupos.common.repo.Protos.File;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.lang.Math;
import java.lang.StringBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Text differencer for finding the diff between 2 text documents. */
public class TextDifferencer {
  private static final String DIFF_TAGS_REGEX = "(\\{\\+|\\+\\}|\\[\\-|\\-\\])";
  private static final Pattern DIFF_TAGS_PATTERN = Pattern.compile(DIFF_TAGS_REGEX);
  static final Map<String, DiffTag.Type> TAG_MAP = ImmutableMap.of(
    "{+", DiffTag.Type.OPEN_ADD,
    "+}", DiffTag.Type.CLOSE_ADD,
    "[-", DiffTag.Type.OPEN_DELETE,
    "-]", DiffTag.Type.CLOSE_DELETE
  );

  @Inject
  public TextDifferencer() {}

  private DiffPatchMatchChange convertToProto(DiffMatchPatch.Diff diff) {
    return DiffPatchMatchChange.newBuilder()
        .setText(diff.text)
        .setType(getChangeType(diff.operation))
        .build();
  }

  private ImmutableList<DiffPatchMatchChange> getDiffPatchMatchChanges(
      String leftContents, String rightContents) {
    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(leftContents, rightContents);
    diffMatchPatch.diff_cleanupSemantic(diffs);
    List<DiffPatchMatchChange> result = new ArrayList<DiffPatchMatchChange>();
    ChangeType previousChangeType = ChangeType.NO_CHANGE;
    for (DiffMatchPatch.Diff diff : diffs) {
      DiffPatchMatchChange change = convertToProto(diff);
      if (change.getType() == ChangeType.NO_CHANGE
          || previousChangeType == ChangeType.NO_CHANGE
          || previousChangeType == change.getType()) {
        result.add(change);
      } else {
        // Change last change to REPLACE:
        DiffPatchMatchChange.Builder replaceChange =
            makeReplaceChange(result.get(result.size() - 1)).toBuilder();
        if (change.getType() == ChangeType.ADD) {
          replaceChange.setReplacingText(replaceChange.getReplacingText() + change.getText());
        } else { // ChangeType.DELETE
          replaceChange.setText(replaceChange.getText() + change.getText());
        }
        result.set(result.size() - 1, replaceChange.build());
      }
      previousChangeType = change.getType();
    }
    normalize(result);
    return ImmutableList.copyOf(result);
  }

  // Normalize changes so they look better
  private void normalize(List<DiffPatchMatchChange> changes) {
    for (int i = 0; i < changes.size() - 1; i++) {
      DiffPatchMatchChange change = changes.get(i);
      if (change.getType() == ChangeType.REPLACE) {
        DiffPatchMatchChange nextChange = changes.get(i + 1);
        // If we're REPLACE and either text or replacing_text doesn't end in \n, and the next
        // change is NO_CHANGE, and it starts with \n, then move the \n to the REPLACE change.
        // Example:
        // REPLACE: text="aaa", replacing_text="bbb\n"
        // NO_CHANGE: text="\nccc\n"
        // After normalization:
        // REPLACE: text="aaa\n", replacing_text="bbb\n\n"
        // NO_CHANGE: text="ccc\n"
        if (nextChange.getType() == ChangeType.NO_CHANGE
            && nextChange.getText().startsWith("\n")
            && (!change.getText().endsWith("\n") || !change.getReplacingText().endsWith("\n"))) {
          changes.set(
              i,
              change
                  .toBuilder()
                  .setText(change.getText() + "\n")
                  .setReplacingText(change.getReplacingText() + "\n")
                  .build());
          changes.set(
              i + 1, nextChange.toBuilder().setText(nextChange.getText().substring(1)).build());
        }
      }
    }
  }

  private DiffPatchMatchChange makeReplaceChange(DiffPatchMatchChange change) {
    DiffPatchMatchChange.Builder replaceChange = change.toBuilder();
    if (change.getType() == ChangeType.ADD) {
      // Move text to replacing_text field
      replaceChange.setText("");
      replaceChange.setReplacingText(change.getText());
    } else { // ChangeType.DELETE or ChangeType.REPLACE
      // Do nothing, text is already in the correct field
    }
    replaceChange.setType(ChangeType.REPLACE);
    return replaceChange.build();
  }

  private ImmutableList<TextChange> getSingleSideChanges(
      ImmutableList<DiffPatchMatchChange> changes, ChangeType sideOperation) {
    if (changes.isEmpty()) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<TextChange> result = ImmutableList.builder();
    StringBuilder sb = new StringBuilder();
    int lineNumber = 0;
    int lineIndex = 0;
    System.out.println("*************** CHANGES **************");
    System.out.println(changes);

    for (DiffPatchMatchChange change : changes) {
      ChangeType changeType = change.getType();
      String changeText = change.getText();
      if (sideOperation == ChangeType.ADD && change.getType() == ChangeType.REPLACE) {
        changeText = change.getReplacingText();
      }
      if (change.getType() == ChangeType.REPLACE) {
        // REPLACE acts as a "joker" - it's like both ADD and DELETE. We set it here to
        // sideOperation since it simplifies the code instead of checking for both every time.
        changeType = sideOperation;
      }
      for (int i = 0; i < changeText.length(); i++) {
        char currentChar = changeText.charAt(i);
        if (currentChar == '\n') {
          if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
            result.add(getTextChange(sb.toString(), lineNumber, lineIndex, changeType));
          } else {
            result.add(
                TextChange.newBuilder()
                    .setType(ChangeType.LINE_PLACEHOLDER)
                    .setLineNumber(lineNumber)
                    .build());
          }
          lineNumber++;
          sb = new StringBuilder();
          lineIndex = 0;
        } else { // char != '/n'
          if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
            lineIndex++;
          }
          sb.append(currentChar);
        }
      }
      // Check for leftover from last line
      if (sb.length() > 0) {
        if (changeType == ChangeType.NO_CHANGE || changeType == sideOperation) {
          result.add(getTextChange(sb.toString(), lineNumber, lineIndex, changeType));
        }
      }
      // Add placeholders for REPLACE
      if (change.getType() == ChangeType.REPLACE) {
        int addPlaceholderCount =
            CharMatcher.is('\n').countIn(change.getText())
                - CharMatcher.is('\n').countIn(change.getReplacingText());
        if (sideOperation == ChangeType.DELETE) {
          addPlaceholderCount *= -1;
        }
        addPlaceholderCount = Math.max(addPlaceholderCount, 0);
        for (int i = 0; i < addPlaceholderCount; i++) {
          result.add(
              TextChange.newBuilder()
                  .setType(ChangeType.LINE_PLACEHOLDER)
                  .setLineNumber(lineNumber + i)
                  .build());
        }
        lineNumber += addPlaceholderCount;
      }
      sb = new StringBuilder();
    }
    return normalize(result.build());
  }

  // Normalize changes, e.g combine subsequent changes of the same type
  private ImmutableList<TextChange> normalize(ImmutableList<TextChange> changes) {
    ArrayList<TextChange> result = new ArrayList(changes);
    for (int i = 0; i < result.size() - 1; i++) {
      TextChange change = result.get(i);
      TextChange nextChange = result.get(i + 1);
      if (change.getType() == nextChange.getType()
          && change.getLineNumber() == nextChange.getLineNumber()
          && change.getEndIndex() == nextChange.getStartIndex()) {
        // Merge changes
        result.set(
            i,
            change
                .toBuilder()
                .setText(change.getText() + nextChange.getText())
                .setEndIndex(nextChange.getEndIndex())
                .build());
        result.remove(i + 1);
      }
    }
    return ImmutableList.copyOf(result);
  }

  private TextChange getTextChange(
      String text, int lineNumber, int lineIndex, ChangeType changeType) {
    return TextChange.newBuilder()
        .setText(text)
        .setType(changeType)
        .setLineNumber(lineNumber)
        .setStartIndex(lineIndex - text.length())
        .setEndIndex(lineIndex)
        .build();
  }

  private boolean isOpen(DiffTag tag) {
    return (tag.getType() == DiffTag.Type.OPEN_ADD || tag.getType() == DiffTag.Type.OPEN_DELETE);
  }

  private boolean isClose(DiffTag tag) {
    return (tag.getType() == DiffTag.Type.CLOSE_ADD || tag.getType() == DiffTag.Type.CLOSE_DELETE);
  }

  private boolean isAdd(DiffTag tag) {
    return (tag.getType() == DiffTag.Type.OPEN_ADD || tag.getType() == DiffTag.Type.CLOSE_ADD);
  }

  private boolean isDelete(DiffTag tag) {
    return (tag.getType() == DiffTag.Type.OPEN_DELETE || tag.getType() == DiffTag.Type.CLOSE_DELETE);
  }

  private boolean tagsBothAddOrBothDelete(DiffTag tag1, DiffTag tag2) {
    return ((isAdd(tag1) && isAdd(tag2)) || (isDelete(tag1) && isDelete(tag2)));
  }

  // Is this whole line a single change, with no characters unchanged?
  private boolean isFullLineChange(DiffLine diffLine) {
    return diffLine.getTagList().size() == 2 && diffLine.getTagList().get(0).getIndex() == 0 && diffLine.getTagList().get(1).getIndex() == diffLine.getText().length();
  }  

  private DiffLine getDiffLine(String diffLineString) {
    // We assume original code does not have the escape sequences {+,+},{-,-}
    DiffLine.Builder result = DiffLine.newBuilder();
    result.setText(diffLineString.replaceAll(DIFF_TAGS_REGEX, ""));
    Matcher matcher = DIFF_TAGS_PATTERN.matcher(diffLineString);
    int matchedTags = 0;
    while (matcher.find()) {
      // We correct the index to the index of the string without the tags.
      int tagIndex = matcher.start() - matchedTags * 2;
      result.addTag(DiffTag.newBuilder().setIndex(tagIndex).setType(TAG_MAP.get(matcher.group())).build());
      matchedTags += 1;
    }
    DiffLine diffLine = result.build();
    // Validate diffLine:
    validateDiffLine(diffLine);
    return diffLine;
  }

  private void validateDiffLine(DiffLine diffLine) {
    if (diffLine.getTagList().size() % 2 == 1) {
      throw new IllegalStateException("Line should have an even number of tags");
    }
    for (int i = 0; i < diffLine.getTagList().size(); i++) {
      DiffTag tag = diffLine.getTagList().get(i);
      if (i % 2 == 0 && isClose(tag)) {
        throw new IllegalStateException(
            "Even tags should be open, but tag " + i + " is " + tag.getType() + " for line:\n" + diffLine);
      }
      if (i % 2 == 1 && isOpen(tag)) {
        throw new IllegalStateException(
            "Even tags should be close, but tag " + i + " is " + tag.getType() + " for line:\n" + diffLine);
      }  
      if (i % 2 == 1 && i > 0) {
        DiffTag prevTag = diffLine.getTagList().get(i - 1);
        if (!tagsBothAddOrBothDelete(prevTag, tag)) {
          throw new IllegalStateException(
            "Tags at " + (i - 1) + " and " + i + " should both be ADD or DELETE, but are " + prevTag.getType() + " and " + tag.getType() + " for line:\n" + diffLine);
        }
      }
    }
  }

  public TextDiff getTextDiff2(String leftText, String rightText, String gitTextWordDiff) {
    if (leftText.matches(DIFF_TAGS_REGEX)) {
      throw new IllegalArgumentException("Left text should not contain {+,+},{-,-}");
    }
    if (rightText.matches(DIFF_TAGS_REGEX)) {
      throw new IllegalArgumentException("Right text should not contain {+,+},{-,-}");
    }
    TextDiff.Builder result = TextDiff.newBuilder();
    int lineNumber = 0;
    for (String line : gitTextWordDiff.split("\n")) {
      DiffLine diffLine = getDiffLine(line);
      System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOO\n" + diffLine);
      // Counters for add and delete segments, to corrent indices
      int addCharCount = 0;
      int deleteCharCount = 0;
      for (int i = 0; i < diffLine.getTagList().size(); i += 2) {
        DiffTag tag1 = diffLine.getTagList().get(i);
        DiffTag tag2 = diffLine.getTagList().get(i + 1);
        String text = diffLine.getText().substring(tag1.getIndex(), tag2.getIndex());
        ChangeType type = isAdd(tag1) ? ChangeType.ADD : ChangeType.DELETE;
        // index is relative to text, which includes both ADD and DELETE segments. We use the
        // offset to correct the index for an ADD-only or DELETE-only string.
        int indexOffset = isAdd(tag1) ? deleteCharCount : addCharCount;
        TextChange change = TextChange.newBuilder()
            .setText(text)
            .setType(type)
            .setLineNumber(lineNumber)
            .setStartIndex(tag1.getIndex() - indexOffset)
            .setEndIndex(tag2.getIndex() - indexOffset)
            .build();
        if (type == ChangeType.ADD) {
          result.addRightChange(change);
          addCharCount += change.getText().length();
        } else {
          result.addLeftChange(change);
          deleteCharCount += change.getText().length();
        }
        
      }
      lineNumber++;
    }

    result.setLeftFileContents(leftText)
        .setRightFileContents(rightText)
        .build();
    System.out.println("*************** TEXT DIFF **************");
    System.out.println(result);
    return result.build();
  }

  public TextDiff getTextDiff(String leftContents, String rightContents) {
    if (leftContents.isEmpty() && rightContents.isEmpty()) {
      return TextDiff.getDefaultInstance();
    }
    ImmutableList<DiffPatchMatchChange> changes =
        getDiffPatchMatchChanges(leftContents, rightContents);

    TextDiff diff =
        TextDiff.newBuilder()
            .addAllLeftChange(getSingleSideChanges(changes, ChangeType.DELETE))
            .addAllRightChange(getSingleSideChanges(changes, ChangeType.ADD))
            .setLeftFileContents(leftContents)
            .setRightFileContents(rightContents)
            .build();
    System.out.println("*************** DIFF **************");
    System.out.println(diff);
    return diff;
  }

  private ChangeType getChangeType(Operation operation) {
    if (operation == Operation.EQUAL) {
      return ChangeType.NO_CHANGE;
    } else if (operation == Operation.INSERT) {
      return ChangeType.ADD;
    } else if (operation == Operation.DELETE) {
      return ChangeType.DELETE;
    } else {
      throw new IllegalArgumentException("Unknown Operation enum: " + operation);
    }
  }
}

