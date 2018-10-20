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

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;

import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;

import java.io.IOException;

/*
 * To read Diff:
 *  bazel run //tools/reviewer/job/sync:github_sync_tool -- read --repo_owner=<repo_owner> --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
 *
 * To write Diff:
 * bazel run //tools/reviewer/job/sync:github_sync_tool -- write --repo_owner=<repo_owner> --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
 */
public class GithubSync {
  // TODO: Add checking input Flags
  @FlagDesc(name = "repo_owner", description = "GitHub repository owner")
  private static Flag<String> repoOwner = Flag.create("");

  @FlagDesc(name = "repo_name", description = "GitHub repository name")
  private static Flag<String> repoName = Flag.create("");

  @FlagDesc(name = "diff_number", description = "GitHub PullRequest number")
  private static Flag<Integer> diffNumber = Flag.create(0);

  @FlagDesc(name = "login", description = "GitHub login")
  private static Flag<String> login = Flag.create("");

  @FlagDesc(name = "password", description = "GitHub password")
  private static Flag<String> password = Flag.create("");

  @FlagDesc(name = "reviewer_diff_link", description = "Base url to reviewer diff")
  private static Flag<String> reviewerDiffLink =
      Flag.create("https://startupos-5f279.firebaseapp.com/diff/");

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    GithubSync githubSync = new GithubSync();
    GithubClient githubClient = new GithubClient(login.get(), password.get());

    if (args.length != 0) {
      if (args[0].equals("read")) {
        githubSync.readPullRequest(githubClient);
      } else if (args[0].equals("write")) {
        // Use real PullRequest instead default instance
        PullRequest pullRequest = PullRequest.getDefaultInstance();
        githubSync.writePullRequest(githubClient, pullRequest);
      }
    }
  }

  private PullRequest readPullRequest(GithubClient githubClient) throws IOException {
    GithubReader reader = new GithubReader(githubClient);
    GithubPullRequestProtos.PullRequest diff =
        reader.getPullRequest(repoOwner.get(), repoName.get(), diffNumber.get());
    System.out.println(diff);
    return diff;
  }

  private void writePullRequest(GithubClient githubClient, PullRequest pullRequest) {
    GithubWriter writer = new GithubWriter(githubClient);
    writer.writePullRequest(pullRequest, repoOwner.get(), repoName.get());
  }
}

