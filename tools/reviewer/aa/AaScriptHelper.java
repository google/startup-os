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

package com.google.startupos.tools.reviewer.aa;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.HttpUtils;
import dagger.Component;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** Tool with some helpful functions for aa script (aa_tool.sh) */
@Singleton
public class AaScriptHelper {
  private final FileUtils fileUtils;
  private final HttpUtils httpUtils;
  private final AaTool aaTool;
  private final String headPath;
  private final String workspacesPath;

  @Inject
  AaScriptHelper(
      FileUtils fileUtils,
      HttpUtils httpUtils,
      AaTool aaTool,
      @Named("Head path") String headPath,
      @Named("Workspaces path") String workspacesPath) {
    this.fileUtils = fileUtils;
    this.httpUtils = httpUtils;
    this.aaTool = aaTool;
    this.headPath = headPath;
    this.workspacesPath = workspacesPath;
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  public interface ToolComponent {
    AaScriptHelper getAaScriptHelper();
  }

  private String getMatches(ImmutableList<String> words, String word) {
    return words.stream()
        .filter(x -> x.startsWith(word))
        .sorted()
        .collect(Collectors.joining("\n"));
  }

  private String getBashCompletions(String previousWord, String currentWord) throws IOException {
    ImmutableList<String> commands = aaTool.getCommands();
    // TODO: Get flags from commands. Perhaps AaTool can provide a get method for it.
    ImmutableList<String> initOptions =
        ImmutableList.of("--base_path", "--startupos_repo", "--user");
    ImmutableList<String> addRepoOptions = ImmutableList.of("--url", "--name");
    ImmutableList<String> diffOptions =
        ImmutableList.of("--reviewers", "--description", "--buglink");

    if (previousWord.equals("aa")) {
      return getMatches(commands, currentWord);
    } else if (previousWord.equals("workspace") || previousWord.equals("aaw")) {
      return getMatches(fileUtils.listSubfolders(workspacesPath), currentWord);
    } else if (commands.contains(previousWord)) {
      // Complete flags
      switch (previousWord) {
        case "init":
          return getMatches(initOptions, currentWord);
        case "add_repo":
          return getMatches(addRepoOptions, currentWord);
        case "diff":
          return getMatches(diffOptions, currentWord);
        default:
          break;
      }
    }
    return "";
  }

  // bazelWorkspacePath is base/head/startup-os or base/ws/<some workspace>/startup-os
  private void startServer(String bazelWorkspacePath) throws IOException {
    if (!checkServer()) {
      // TODO: Flags don't seem to work. Figure out and set --logToFile here and default to false.
      String[] command = new String[] {"bazel-bin/tools/reviewer/local_server/local_server", "&"};
      Runtime.getRuntime()
          .exec(
              command,
              new String[] {"BUILD_WORKSPACE_DIRECTORY=" + bazelWorkspacePath},
              new File(bazelWorkspacePath));
      // TODO: Use AuthService.DEBUGGING_TOKEN_PATH. Consider moving it to a common location.
      if (!fileUtils.fileExists("~/aa_token")) {
        System.out.println("Visit https://web-login-startupos.firebaseapp.com to log in");
      }
      try {
        // Wait until server is up
        while (!checkServer()) {
          Thread.sleep(50);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  // bazelWorkspacePath is base/head/startup-os or base/ws/<some workspace>/startup-os
  private boolean checkServer() {
    try {
      return "OK".equals(httpUtils.get("http://localhost:7000/health"));
    } catch (Exception e) {
      return false;
    }
  }

  private void check(boolean predicateResult, String message) throws IllegalArgumentException {
    if (!predicateResult) {
      throw new IllegalArgumentException(message);
    }
  }

  private boolean run(String[] args) {
    try {
      check(args.length > 0, "Please specify command");
      String command = args[0];
      switch (command) {
        case "completions":
          check(args.length == 3, "Incorrect args. Use: 'completions previousWord currentWord'");
          System.out.print(getBashCompletions(args[1], args[2]));
          break;
        case "head_path":
          check(args.length == 1, "Incorrect args. Use: 'head_path'");
          System.out.print(headPath);
          break;
        case "start_server":
          check(args.length == 2, "Incorrect args. Use: 'start_server bazel_workspace_path'");
          startServer(args[1]);
          break;
        case "check_server":
          check(args.length == 1, "Incorrect args. Use: 'check_server'");
          checkServer();
          break;
        default:
          return false;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void main(String[] args) {
    if (!DaggerAaScriptHelper_ToolComponent.create().getAaScriptHelper().run(args)) {
      System.exit(1);
    }
  }
}

