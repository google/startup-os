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

import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.reviewer.localserver.service.Protos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Usage:
 * bazel build //tools/reviewer/job/sync:sync_tool
 * bazel run //tools/reviewer/job/sync:sync_tool
 *
 * After running the program follow instructions
 * (enter 'repository_name', 'Pull Request number',  'login' and 'password').
 */
public class SyncTool {
  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);

        String repo;
        int diffNumber;
        String login;
        String password;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
          System.out.print("Enter GitHub repository name:\n");
          repo = br.readLine();
          System.out.print("Enter Pull Request number:\n");
          diffNumber = Integer.parseInt(br.readLine());
          System.out.print("Enter GitHub login:\n");
          login = br.readLine();
          System.out.print("Enter GitHub password:\n");
          password = br.readLine();
        }

    RecipientGitHubDiff recipientGitHubComments =
        new RecipientGitHubDiff(
            repo, diffNumber, login, password);

    Protos.Diff diff = recipientGitHubComments.getDiff();
    System.out.println(diff);
  }
}

