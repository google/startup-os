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

import com.google.startupos.common.CommonModule;
import dagger.Component;
import dagger.Lazy;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.inject.Named;
import java.io.IOException;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.HttpUtils;
import java.io.File;

/** Tool with some helpful functions for aa script (aa_tool.sh) */
@Singleton
public class AaScriptHelper {
  private FileUtils fileUtils;
  private HttpUtils httpUtils;
  private AaTool aaTool;
  private String basePath;
  private String headPath;

  @Inject
  AaScriptHelper(
      FileUtils fileUtils,
      HttpUtils httpUtils,
      AaTool aaTool,
      @Named("Base path") String basePath,
      @Named("Head path") String headPath) {
    this.fileUtils = fileUtils;
    this.httpUtils = httpUtils;
    this.aaTool = aaTool;
    this.basePath = basePath;
    this.headPath = headPath;
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  public interface ToolComponent {
    AaScriptHelper getAaScriptHelper();
  }

  private String getBashCompletions(String previousWord, String currentWord) {
    String commands = "init workspace diff fix sync snapshot add_repo killserver";
    String init_options = "--base_path --startupos_repo --user";
    String add_repo_options = "--url --name";
    String diff_options = "--reviewers --description --buglink";
    String command = null;

    if (previousWord.equals("aa")) {
      return String.format("compgen -W \"%s\" -- \"%s\"", commands, currentWord);
    } else if (previousWord.equals("workspace") || previousWord.equals("aaw")) {
      // XXX get real workspaces
      String workspaces = "aaa bbb ccc";
      return String.format("compgen -W \"%s\" -- %s", workspaces, currentWord);
    } else if (commands.contains(previousWord)) { // XXX match whole word only
      command = previousWord;
    } else {
      return "";
    }
    // XXX Replace with bash code:
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
  private boolean checkServer() throws IOException {
    try {
      return "OK".equals(httpUtils.get("http://localhost:7000/health"));
    } catch (Exception e) {
      return false;
    }
  }

  private void check(boolean predicate, String message) throws IllegalArgumentException {
    if (!predicate) {
      throw new IllegalArgumentException(message);
    }
  }

  private boolean run(String[] args) {
    try {
      check(args.length > 0, "Please specify command");
      String command = args[0];
      if (command.equals("completions")) {
        check(args.length == 3, "Incorrect args. Use: 'completions previousWord currentWord'");
        System.out.print(getBashCompletions(args[1], args[2]));
      } else if (command.equals("head_path")) {
        check(args.length == 1, "Incorrect args. Use: 'head_path'");
        System.out.print(headPath);
      } else if (command.equals("start_server")) {
        check(args.length == 2, "Incorrect args. Use: 'start_server bazel_workspace_path'");
        startServer(args[1]);
      } else if (command.equals("check_server")) {
        check(args.length == 1, "Incorrect args. Use: 'check_server'");
        checkServer();
      } else {
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

