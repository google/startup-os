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
import com.google.startupos.tools.reviewer.localserver.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;

// TODO: Implement methods
public class ReviewerClient {
  private final CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;

  public ReviewerClient() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 8001).usePlaintext(true).build();
    blockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  public Diff getDiff(long diffNumber) {
    DiffRequest request = DiffRequest.newBuilder().setDiffId(diffNumber).build();
    return blockingStub.getDiff(request);
  }

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
      long diffId, String reviewerThreadId, long githubCommentId, String reviewerCommentId) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the id *%s* of GitHub review comment to the Reviewer "
                + "code comment(thread_id: %s, comment_id: %s)",
            diffId, githubCommentId, reviewerThreadId, reviewerCommentId);
  }

  public void addGithubReviewCommentPosition(
      long diffId, String reviewerThreadId, int githubCommentPosition, String reviewerCommentId) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the comment position *%s* of GitHub review comment to the Reviewer "
                + "code comment(thread_id: %s, comment_id: %s)",
            diffId, githubCommentPosition, reviewerThreadId, reviewerCommentId);
  }

  public void updateCodeComment(
      long diffId,
      String reviewerThreadId,
      String reviewerCommentId,
      long githubReviewCommentId,
      String content) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Updated Reviewer code comment(thread_id: %s, comment_id: %s, github_comment_id: %s). New content: %s",
            diffId, reviewerThreadId, reviewerCommentId, githubReviewCommentId, content);
  }

  public void deleteCodeComment(
      long diffId, String reviewerThreadId, String reviewerCommentId, long githubReviewCommentId) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Deleted Reviewer code comment(thread_id: %s, comment_id: %s, github_comment_id: %s)",
            diffId, reviewerThreadId, reviewerCommentId, githubReviewCommentId);
  }

  public void deleteThreadComment(
      long diffId, String reviewerThreadId, String reviewerCommentId, long githubReviewCommentId) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Deleted Reviewer thread comment(thread_id: %s, comment_id: %s, github_comment_id: %s)",
            diffId, reviewerThreadId, reviewerCommentId, githubReviewCommentId);
  }

  public void addCodeComment(long diffId, ReviewComment comment) {
    log.atInfo()
        .log("Diff with id *%s* is updated. Added new Reviewer code comment: %s", diffId, comment);
  }

  public void addGithubIssueCommentId(
      long diffId, String reviewerThreadId, long githubIssueCommentId, String reviewerCommentId) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Added the id *%s* of GitHub issue comment to the Reviewer "
                + "diff comment(thread_id: %s, comment_id: %s)",
            diffId, githubIssueCommentId, reviewerThreadId, reviewerCommentId);
  }

  public void updateDiffComment(
      long diffId,
      String reviewerThreadId,
      String reviewerCommentId,
      long githubIssueCommentId,
      String content) {
    log.atInfo()
        .log(
            "Diff with id *%s* is updated. "
                + "Updated Reviewer diff comment(thread_id: %s, comment_id: %s, github_comment_id: %s). New content: %s",
            diffId, reviewerThreadId, reviewerCommentId, githubIssueCommentId, content);
  }

  public void addDiffComment(long diffId, IssueComment comment) {
    log.atInfo()
        .log("Diff with id *%s* is updated. Added new Reviewer diff comment: %s", diffId, comment);
  }
}

