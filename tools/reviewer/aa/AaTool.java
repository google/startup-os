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
import com.google.startupos.tools.reviewer.aa.commands.AaCommand;
import com.google.startupos.tools.reviewer.aa.commands.AddRepoCommand;
import com.google.startupos.tools.reviewer.aa.commands.DiffCommand;
import com.google.startupos.tools.reviewer.aa.commands.FixCommand;
import com.google.startupos.tools.reviewer.aa.commands.InitCommand;
import com.google.startupos.tools.reviewer.aa.commands.KillServerCommand;
import com.google.startupos.tools.reviewer.aa.commands.PatchCommand;
import com.google.startupos.tools.reviewer.aa.commands.ReviewCommand;
import com.google.startupos.tools.reviewer.aa.commands.SnapshotCommand;
import com.google.startupos.tools.reviewer.aa.commands.SubmitCommand;
import com.google.startupos.tools.reviewer.aa.commands.SyncCommand;
import com.google.startupos.tools.reviewer.aa.commands.WorkspaceCommand;
import dagger.Component;
import dagger.Lazy;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

/** aa tool. */
@Singleton
public class AaTool {
  private final Map<String, Lazy<? extends AaCommand>> commands = new HashMap<>();

  @Inject
  AaTool(
      Lazy<InitCommand> initCommand,
      Lazy<WorkspaceCommand> workspaceCommand,
      Lazy<SyncCommand> syncCommand,
      Lazy<DiffCommand> diffCommand,
      Lazy<FixCommand> fixCommand,
      Lazy<ReviewCommand> reviewCommand,
      Lazy<SnapshotCommand> snapshotCommand,
      Lazy<SubmitCommand> submitCommand,
      Lazy<AddRepoCommand> addRepoCommand,
      Lazy<PatchCommand> patchCommand,
      Lazy<KillServerCommand> killServerCommand) {
    commands.put("init", initCommand);
    commands.put("workspace", workspaceCommand);
    commands.put("sync", syncCommand);
    commands.put("diff", diffCommand);
    commands.put("fix", fixCommand);
    commands.put("review", reviewCommand);
    commands.put("snapshot", snapshotCommand);
    commands.put("submit", submitCommand);
    commands.put("add_repo", addRepoCommand);
    commands.put("patch", patchCommand);
    commands.put("killserver", killServerCommand);
  }

  private void printUsage() {
    System.out.println(
        String.format(
            "Invalid usage. Available commands are: %s", String.join(", ", commands.keySet())));
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  public interface AaToolComponent {
    AaTool getAaTool();
  }

  public ImmutableList<String> getCommands() {
    return ImmutableList.sortedCopyOf(commands.keySet());
  }

  private boolean run(String[] args) {
    if (args.length > 0) {
      String command = args[0];
      if (commands.containsKey(command)) {
        return commands.get(command).get().run(args);
      } else {
        System.out.println();
        printUsage();
      }
    } else {
      printUsage();
    }
    return false;
  }

  public static void main(String[] args) {
    if (!DaggerAaTool_AaToolComponent.create().getAaTool().run(args)) {
      System.exit(1);
    }
  }
}

