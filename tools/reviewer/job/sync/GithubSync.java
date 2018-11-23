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

/*
* To read GitHub PullRequest:
*  bazel run //tools/reviewer/job/sync:github_sync_tool -- read --repo_owner=<repo_owner> --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
*
* To create GitHub PullRequest from scratch
* bazel run //tools/reviewer/job/sync:github_sync_tool -- create --repo_owner=<repo_owner> --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>
*
* To update existing GitHub PullRequest
* bazel run //tools/reviewer/job/sync:github_sync_tool -- update --repo_owner=<repo_owner> --repo_name=<repo name> --diff_number=<diff_number> --login=<GitHub login> --password=<GitHub password>

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

  public static void main(String[] args) {
    Flags.parseCurrentPackage(args);
    // The sync logic will be added in next PRs
  }
}

