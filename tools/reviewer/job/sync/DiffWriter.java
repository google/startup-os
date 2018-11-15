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

import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;

// TODO: Implement methods
public class DiffWriter {
  public void addGithubPrNumber(
      long diffId, String owner, String repo, long githubPullRequestNumber) {
    System.out.println(
        "Diff \'"
            + diffId
            + "\' is updated.\n"
            + "Added GitHub PullRequest number \'"
            + githubPullRequestNumber
            + "\' to GithubPr:\n"
            + "owner - \'"
            + owner
            + "\',\n"
            + "repo - \'"
            + repo
            + "\'\n");
  }

  public void addGithubReviewCommentId(
      long diffId,
      String reviewerThreadId,
      long githubReviewCommentId,
      String createdBy,
      String timestamp) {
    System.out.println(
        "Diff \'"
            + diffId
            + "\' is updated.\n"
            + "Added GitHub Review comment id \'"
            + githubReviewCommentId
            + "\' to code comment:\n"
            + "Reviewer thread id - \'"
            + reviewerThreadId
            + "\',\n"
            + "created by - \'"
            + createdBy
            + "\',\n"
            + "timestamp - \'"
            + timestamp
            + "\'\n");
  }

  public void updateCodeComment(
      long diffId, String reviewerThreadId, long githubReviewCommentId, String content) {
    System.out.println(
        "Diff \'"
            + diffId
            + "\' is updated.\n"
            + "Updated code comment:\n"
            + "Reviewer thread id - \'"
            + reviewerThreadId
            + "\',\n"
            + "GitHub Review Comment id - \'"
            + githubReviewCommentId
            + "\',\n"
            + "comment content - \'"
            + content
            + "\'\n");
  }

  // TODO: Change the method signature to work with reviewer.localserver.service.Protos.Thread
  public void addCodeComment(long diffId, ReviewComment comment) {
    System.out.println(
        "Diff \'" + diffId + "\' is updated.\n" + "Added new code comment: \n" + comment + "\n");
  }

  public void addGithubIssueCommentId(
      long diffId,
      String reviewerThreadId,
      long githubIssueCommentId,
      String createdBy,
      String timestamp) {
    System.out.println(
        "Diff \'"
            + diffId
            + "\' is updated.\n"
            + "Added GitHub Issue comment id \'"
            + githubIssueCommentId
            + "\' to diff comment:\n"
            + "Reviewer thread id - \'"
            + reviewerThreadId
            + "\',\n"
            + "created by - \'"
            + createdBy
            + "\',\n"
            + "timestamp - \'"
            + timestamp
            + "\'\n");
  }

  public void updateDiffComment(
      long diffId, String reviewerThreadId, long githubIssueCommentId, String content) {
    System.out.println(
        "Diff \'"
            + diffId
            + "\' is updated.\n"
            + "Updated diff comment:\n"
            + "Reviewer thread id - \'"
            + reviewerThreadId
            + "\',\n"
            + "GitHub Issue Comment id - \'"
            + githubIssueCommentId
            + "\',\n"
            + "comment content - \'"
            + content
            + "\'\n");
  }

  // TODO: Change the method signature to work with reviewer.localserver.service.Protos.Thread
  public void addDiffComment(long diffId, IssueComment comment) {
    System.out.println(
        "Diff \'" + diffId + "\' is updated.\n" + "Added new diff comment: \n" + comment + "\n");
  }
}

