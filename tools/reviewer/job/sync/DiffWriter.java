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

import com.google.common.flogger.FluentLogger;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;

// TODO: Implement methods
public class DiffWriter {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  public void addGithubPrNumber(
      long diffId, String owner, String repo, long githubPullRequestNumber) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the number *%s* of GitHub Pull Request to Reviewer GithubPr(owner: %s, repo: %s)",
            diffId, githubPullRequestNumber, owner, repo);
  }

  public void addGithubReviewCommentId(
      long diffId,
      String reviewerThreadId,
      long githubReviewCommentId,
      String createdBy,
      String timestamp) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the id *%s* of GitHub review comment to the Reviewer "
                + "code comment(thread id: %s, created_by: %s, timestamp: %s)",
            diffId, githubReviewCommentId, reviewerThreadId, createdBy, timestamp);
  }

  public void updateCodeComment(
      long diffId, String reviewerThreadId, long githubReviewCommentId, String content) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Updated Reviewer code comment(thread id: %s, github_comment_id: %s). New content: %s",
            diffId, reviewerThreadId, githubReviewCommentId, content);
  }

  // TODO: Change the method signature to work with reviewer.localserver.service.Protos.Thread
  public void addCodeComment(long diffId, ReviewComment comment) {
    log.atInfo()
        .log("Diff with id *%s* is updated. Added new Reviewer code comment: %s", diffId, comment);
  }

  public void addGithubIssueCommentId(
      long diffId,
      String reviewerThreadId,
      long githubIssueCommentId,
      String createdBy,
      String timestamp) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the id *%s* of GitHub issue comment to the Reviewer "
                + "diff comment(thread id: %s, created_by: %s, timestamp: %s)",
            diffId, githubIssueCommentId, reviewerThreadId, createdBy, timestamp);
  }

  public void updateDiffComment(
      long diffId, String reviewerThreadId, long githubIssueCommentId, String content) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Updated Reviewer diff comment(thread id: %s, github_comment_id: %s). New content: %s",
            diffId, reviewerThreadId, githubIssueCommentId, content);
  }

  // TODO: Change the method signature to work with reviewer.localserver.service.Protos.Thread
  public void addDiffComment(long diffId, IssueComment comment) {
    log.atInfo()
        .log("Diff with id *%s* is updated. Added new Reviewer diff comment: %s", diffId, comment);
  }
}

