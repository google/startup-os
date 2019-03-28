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

package com.google.startupos.tools.reviewer.rules_updater;

import com.google.startupos.common.DaggerCommonComponent;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.reviewer.ReviewerProtos;
import com.google.startupos.tools.reviewer.ReviewerProtos.ReviewerConfig;

import java.nio.file.Paths;
import java.util.stream.Collectors;

class RulesUpdater {
  @FlagDesc(name = "config", description = "ReviewerConfig to read from", required = true)
  public static Flag<String> config = Flag.create("");

  @FlagDesc(name = "output", description = "Rules file to output to", required = true)
  public static Flag<String> output = Flag.create("");

  private static String RULES_TEMPLATE =
      "service cloud.firestore {\n"
          + "\tmatch /databases/{database}/documents {\n"
          + "\t\tmatch /{document=**} {\n"
          + "\t\t\tallow read: if true;\n"
          + "\t\t\tallow write: if %s;\n"
          + "\t\t}\n"
          + "\t}\n"
          + "}\n";

  private static String EMAIL_CLAUSE = "request.auth.token.email == '%s'";

  public static void main(String[] args) {
    Flags.parseCurrentPackage(args);
    FileUtils fileUtils = DaggerCommonComponent.create().getFileUtils();
    String configPath;
    String outputPath;

    String buildWorkspaceDirectory = System.getenv("BUILD_WORKSPACE_DIRECTORY");

    if (Paths.get(config.get()).isAbsolute()) {
      configPath = config.get();
    } else {
      configPath = Paths.get(buildWorkspaceDirectory, config.get()).toAbsolutePath().toString();
    }

    if (Paths.get(output.get()).isAbsolute()) {
      outputPath = output.get();
    } else {
      outputPath = Paths.get(buildWorkspaceDirectory, output.get()).toAbsolutePath().toString();
    }

    ReviewerConfig reviewerConfig =
        (ReviewerConfig) fileUtils.readPrototxtUnchecked(configPath, ReviewerConfig.newBuilder());

    String rules =
        String.format(
            RULES_TEMPLATE,
            reviewerConfig.getUserList().stream()
                .map(ReviewerProtos.User::getEmail)
                .map(email -> String.format(EMAIL_CLAUSE, email))
                .collect(Collectors.joining(" || ")));

    fileUtils.writeStringUnchecked(rules, outputPath);
  }
}

