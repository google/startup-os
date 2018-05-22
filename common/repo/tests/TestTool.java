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
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;


/** Test tool for GitRepo. */
@Singleton
public class TestTool {
  private static String REPO_ROOT_PATH = "";
  private static String COMMIT_ID = "";
  private static String FILE_PATH = "WORKSPACE";

  private GitRepoFactory repoFactory;

  @Inject
  TestTool(GitRepoFactory repoFactory) {
    this.repoFactory = repoFactory;
  }

  public String getFile(String repoPath, String commitId, String path) {
    GitRepo repo = repoFactory.create(repoPath);
    return repo.getFileContents(commitId, path);
  }

  @Singleton
  @Component(modules = { CommonModule.class })
  public interface TestToolComponent {
    TestTool getTestTool();
  }

  public static void main(String[] args) {
    if (REPO_ROOT_PATH.isEmpty() || COMMIT_ID.isEmpty()) {
      System.out.println("Please set REPO_ROOT_PATH and COMMIT_ID");
      System.exit(1);
    }
    TestTool tool = DaggerTestTool_TestToolComponent.create().getTestTool();
    String content = tool.getFile(
        REPO_ROOT_PATH,
        COMMIT_ID,
        FILE_PATH);
    System.out.println(content);
  }
}
