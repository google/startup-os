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

/** Tool with some helpful functions for aa script (aa_tool.sh) */
@Singleton
public class AaScriptHelper {
  private AaTool aaTool;
  private String basePath;
  private String headPath;

  @Inject
  AaScriptHelper(
      AaTool aaTool, @Named("Base path") String basePath, @Named("Head path") String headPath) {
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
      return String.format("compgen -W \"%s\" -- %s", commands, previousWord);
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

  private boolean run(String[] args) {
    if (args.length > 0) {
      String command = args[0];
      if (command.equals("completions")) {
        if (args.length != 3) {
          return false;
        }
        System.out.print(getBashCompletions(args[1], args[2]));
        return true;
      } else if (command.equals("head_path")) {
        System.out.print(headPath);
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    if (!DaggerAaScriptHelper_ToolComponent.create().getAaScriptHelper().run(args)) {
      System.exit(1);
    }
  }
}

