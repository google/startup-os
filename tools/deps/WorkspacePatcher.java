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

package com.google.startupos.tools.workspace_patcher;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WorkspacePatcher {
  // We need to patch WORKSPACE file to replace
  // external repo references with local repos
  @FlagDesc(name = "workspace", description = "WORKSPACE file to patch", required = true)
  private static final Flag<String> workspace = Flag.create(".");

  public static String hasadnaPatch(String workpaceContents) {
    // Replaces `http_archive` link to google/startup-os
    // with `local_repository` in `hasadna/hasadna` WORKSPACE file
    return workpaceContents.replaceAll(
        // (?s) enables multiline matching
        "(?s)# MARK: StartupOS start.*# MARK: StartupOS end",
        "local_repository(name=\"startup_os\",path=\"/home/circleci/ng/\")");
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);

    String workspaceContents = new String(Files.readAllBytes(Paths.get(workspace.get())));
    workspaceContents = hasadnaPatch(workspaceContents);
    Files.write(Paths.get(workspace.get()), workspaceContents.getBytes(UTF_8));
  }
}

