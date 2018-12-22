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

package com.google.startupos.tools.reviewer.job.sync;

import com.google.common.collect.ImmutableMap;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GithubSyncTool {
  // TODO: Add checking input Flags
  @FlagDesc(name = "repo_paths", description = "Git repository paths")
  private static final Flag<List<String>> repoPaths = Flag.createStringsListFlag(Arrays.asList());

  @FlagDesc(name = "diff_number", description = "Reviewer diff number")
  private static Flag<Integer> diffNumber = Flag.create(0);

  @FlagDesc(name = "login", description = "GitHub login")
  private static Flag<String> login = Flag.create("");

  @FlagDesc(name = "password", description = "GitHub password")
  private static Flag<String> password = Flag.create("");

  @FlagDesc(name = "reviewer_diff_link", description = "Base url to reviewer diff")
  private static Flag<String> reviewerDiffLink =
      Flag.create("https://startupos-5f279.firebaseapp.com/diff/");

  private GitRepoFactory gitRepoFactory;

  @Inject
  public GithubSyncTool(GitRepoFactory gitRepoFactory) {
    this.gitRepoFactory = gitRepoFactory;
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface GitubSyncToolComponent {
    GitRepoFactory getGitRepoFactory();
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    GitRepoFactory gitRepoFactory =
        DaggerGithubSyncTool_GitubSyncToolComponent.builder().build().getGitRepoFactory();
    ImmutableMap<String, GitRepo> repoNameToGitRepos =
        getGitReposMap(repoPaths.get(), gitRepoFactory);

    GithubClient githubClient = new GithubClient(login.get(), password.get());

    GithubSync githubSync = new GithubSync(githubClient, reviewerDiffLink.get());
    githubSync.syncWithGithub(diffNumber.get(), repoNameToGitRepos);
  }

  private static ImmutableMap<String, GitRepo> getGitReposMap(
      List<String> repoPaths, GitRepoFactory gitRepoFactory) {
    ImmutableMap.Builder<String, GitRepo> result = ImmutableMap.builder();
    for (String repoPath : repoPaths) {
      String repoFolderName = repoPath.substring(repoPath.lastIndexOf('/') + 1);
      result.put(repoFolderName, gitRepoFactory.create(repoPath));
    }
    return result.build();
  }
}

