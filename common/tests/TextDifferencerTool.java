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
import com.google.startupos.common.TextChange;

/** A tool for testing TextDifferencer. */
@Singleton
public class TextDifferencerTool {
  @FlagDesc(name = "left_file", description = "Left file", required = true)
  public static Flag<String> leftFile = Flag.create("");

  @FlagDesc(name = "right_file", description = "Right file", required = true)
  public static Flag<String> rightFile = Flag.create("");

  private FileUtils fileUtils;
  private TextDifferencer textDifferencer;

  @Inject
  TextDifferencerTool(FileUtils fileUtils, TextDifferencer textDifferencer) {
    this.fileUtils = fileUtils;
    this.textDifferencer = textDifferencer;
  }

  void run() throws IOException {
    String leftFileContents = fileUtils.readFile(leftFile.get());
    String rightFileContents = fileUtils.readFile(rightFile.get());

    for (TextChange textChange :
        textDifferencer.getAllTextChanges(leftFileContents, rightFileContents)) {
      System.out.println(textChange);
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

