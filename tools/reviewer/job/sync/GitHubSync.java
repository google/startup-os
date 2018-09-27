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
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;

import java.io.IOException;

/*
 * To read Diff:
 *  bazel run //tools/reviewer/job/sync:github_sync_tool -- read --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
 *
 * To write Diff:
 * bazel run //tools/reviewer/job/sync:github_sync_tool -- write --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
 */
public class GitHubSync {
  // TODO: Add checking input Flags
  @FlagDesc(name = "repo_name", description = "GitHub repository name")
  private static Flag<String> repoName = Flag.create("");

  @FlagDesc(name = "diff_number", description = "PullRequest number")
  private static Flag<Integer> diffNumber = Flag.create(0);

  @FlagDesc(name = "login", description = "GitHub login")
  private static Flag<String> login = Flag.create("");

  @FlagDesc(name = "password", description = "GitHub password")
  private static Flag<String> password = Flag.create("");

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    GitHubSync gitHubSync = new GitHubSync();
    ReviewerBot bot = new ReviewerBot(login.get(), password.get());
    GitHubClient gitHubClient = new GitHubClient(bot);

    if (args.length != 0) {
      if (args[0].equals("read")) {
        gitHubSync.readDiff(gitHubClient);
      } else if (args[0].equals("write")) {
        // Use real diff instead default instance
        Diff diff = Diff.getDefaultInstance();
        gitHubSync.writeDiff(gitHubClient, diff);
      }
    }
  }

  private void readDiff(GitHubClient gitHubClient) throws IOException {
    GitHubReader reader = new GitHubReader(gitHubClient);
    Diff diff = reader.getDiff(repoName.get(), diffNumber.get());
    System.out.println(diff);
  }

  private void writeDiff(GitHubClient gitHubClient, Diff diff) throws IOException {
    GitHubWriter writer = new GitHubWriter(gitHubClient);
    writer.writeDiff(diff, repoName.get());
  }
}

