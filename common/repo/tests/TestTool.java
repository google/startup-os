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

package com.google.startupos.common.repo.tests;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.common.repo.GitRepoFactory;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.File;


/** Test tool for GitRepo. */
@Singleton
public class TestTool {
  private GitRepoFactory repoFactory;

  @Inject
  TestTool(GitRepoFactory repoFactory) {
    this.repoFactory = repoFactory;
  }

  public void run(String[] args) {
    if (args.length > 0) {
      String command = args[0];
      Repo repo = repoFactory.create(System.getenv("BUILD_WORKSPACE_DIRECTORY"));
      if (command.equals("switchBranch")) {
        String branch = args[1];
        repo.switchBranch(branch);
      } if (command.equals("getCommits")) {
        String branch = args[1];
        for (Commit commit : repo.getCommits(branch)) {
          System.out.println();
          System.out.println(commit);
        }
      } else if (command.equals("getUncommittedFiles")) {
        for (File file : repo.getUncommittedFiles()) {
          System.out.println(file);
        }
      } else if (command.equals("merge")) {
        String branch = args[1];
        repo.merge(branch);
      } else if (command.equals("mergeTheirs")) {
        String branch = args[1];
        repo.mergeTheirs(branch);
      } else if (command.equals("isMerged")) {
        String branch = args[1];
        repo.isMerged(branch);
      } else if (command.equals("removeBranch")) {
        String branch = args[1];
        repo.removeBranch(branch);
      } else if (command.equals("listBranches")) {
        for (String branch : repo.listBranches()) {
          System.out.println(branch);
        }
      } else {
        System.out.println("Unknown command");
      }
    } else {
      System.out.println("Please specify command");
    }
  }

  @Singleton
  @Component(modules = { CommonModule.class })
  public interface TestToolComponent {
    TestTool getTestTool();
  }

  public static void main(String[] args) {
    DaggerTestTool_TestToolComponent.create().getTestTool().run(args);
  }
}
