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

package com.google.startupos.common.tests;

import com.google.startupos.common.CommonModule;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.TextDifferencer;
import java.io.IOException;
import com.google.common.base.Strings;
import com.google.startupos.common.Protos.TextDiff;
import com.google.startupos.common.Protos.TextChange;
import com.google.startupos.common.Protos.ChangeType;
import java.util.List;
import java.util.ArrayList;

/** A tool for testing TextDifferencer. */
@Singleton
public class TextDifferencerTool {
  static final String ANSI_RESET = "\u001B[0m";
  static final String ANSI_WHITE = "\u001B[37m";
  static final String ANSI_RED_BACKGROUND = "\u001B[41m";
  static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
  static final int PADDING = 100;

  @FlagDesc(name = "left_file", description = "Left file", required = true)
  public static Flag<String> leftFile = Flag.create("");

  @FlagDesc(name = "right_file", description = "Right file", required = true)
  public static Flag<String> rightFile = Flag.create("");

  private FileUtils fileUtils;
  private TextDifferencer textDifferencer;

  public static String unEscapeString(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++)
      switch (s.charAt(i)) {
        case '\n':
          sb.append("\\n");
          break;
        case '\t':
          sb.append("\\t");
          break;
          // ... rest of escape characters
        default:
          sb.append(s.charAt(i));
      }
    return sb.toString();
  }

  @Inject
  TextDifferencerTool(FileUtils fileUtils, TextDifferencer textDifferencer) {
    this.fileUtils = fileUtils;
    this.textDifferencer = textDifferencer;
  }

  private String getLeftText(TextChange change) {
    if (change.getType() == ChangeType.NO_CHANGE) {
      return ANSI_WHITE + change.getText() + ANSI_RESET;
    } else if (change.getType() == ChangeType.DELETE) {
      return ANSI_WHITE + ANSI_RED_BACKGROUND + change.getText() + ANSI_RESET;
    }
    // LINE_PLACEHOLDER
    return "";
  }

  private String getRightText(TextChange change) {
    if (change.getType() == ChangeType.NO_CHANGE) {
      return ANSI_WHITE + change.getText() + ANSI_RESET;
    } else if (change.getType() == ChangeType.ADD) {
      return ANSI_WHITE + ANSI_GREEN_BACKGROUND + change.getText() + ANSI_RESET;
    }
    // LINE_PLACEHOLDER
    return "";
  }

  void run() throws IOException {
    String leftFileContents = fileUtils.readFile(leftFile.get());
    String rightFileContents = fileUtils.readFile(rightFile.get());
    TextDiff textDiff = textDifferencer.getTextDiff(leftFileContents, rightFileContents);
    int iLeft = 0;
    int iRight = 0;
    String leftLine = "";
    String rightLine = "";
    int leftStringLength = 0;
    int lineNumber = 0;
    List<String> leftOutput = new ArrayList<>();
    List<String> rightOutput = new ArrayList<>();
    boolean done = false;
    while (!done) {
      while (iLeft < textDiff.getLeftChangeList().size()
          && textDiff.getLeftChangeList().get(iLeft).getLineNumber() == lineNumber) {
        TextChange change = textDiff.getLeftChangeList().get(iLeft);
        leftLine += getLeftText(change);
        leftStringLength += change.getText().length();
        iLeft++;
        String indexString =
            leftFileContents.substring(change.getGlobalStartIndex(), change.getGlobalEndIndex());
      }
      while (iRight < textDiff.getRightChangeList().size()
          && textDiff.getRightChangeList().get(iRight).getLineNumber() == lineNumber) {
        TextChange change = textDiff.getRightChangeList().get(iRight);
        rightLine += getRightText(change);
        iRight++;
      }
      String padding = Strings.repeat(" ", PADDING - leftStringLength);
      System.out.println(leftLine + padding + rightLine);
      leftLine = "";
      rightLine = "";
      leftStringLength = 0;

      lineNumber++;
      done =
          iLeft == textDiff.getLeftChangeList().size()
              && iRight == textDiff.getRightChangeList().size();
    }
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    TextDifferencerTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parse(args, TextDifferencerTool.class.getPackage());
    DaggerTextDifferencerTool_ToolComponent.create().getTool().run();
  }
}

