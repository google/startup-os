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
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.DeleteIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.DeleteReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.EditIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.EditReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;

/**
 * Writes `tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest` to GitHub using GitHub API
 */
public class GithubWriter {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final GithubClient githubClient;
  private final String reviewerUrl;

  GithubWriter(GithubClient githubClient, String reviewerUrl) {
    this.githubClient = githubClient;
    this.reviewerUrl = reviewerUrl;
  }

  /**
   * Creates GitHub PullRequest from scratch
   *
   * @param pullRequest Proto message from which will be created GitHub PullRequest
   * @param repoOwner Owner of GirHub repository
   * @param repoName Name GirHub repository
   * @return GitHub repository number
   */
  public long createPullRequest(PullRequest pullRequest, String repoOwner, String repoName) {
    long pullRequestNumber =
        githubClient
            .createPullRequest(
                CreatePullRequestRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setRequestData(
                        CreatePullRequestRequest.CreatePullRequestRequestData.newBuilder()
                            .setTitle(pullRequest.getTitle())
                            .setHead(pullRequest.getHeadBranchName())
                            .setBase(pullRequest.getBaseBranchName())
                            // An attempt to create new Pull Request without specifying `body`
                            // causes an error. We set the default value to avoid the error.
                            .setBody(
                                pullRequest.getBody().isEmpty()
                                    ? "No description set"
                                    : pullRequest.getBody())
                            .build())
                    .build())
            .getPullRequest()
            .getNumber();
    log.atInfo()
        .log(
            "Pull Request with number *%s* was CREATED on GitHub(owner: %s, name: %s).",
            pullRequestNumber, repoOwner, repoName);
    return pullRequestNumber;
  }

  public ReviewComment createReviewComment(
      long pullRequestNumber, ReviewComment reviewComment, PullRequest pullRequest) {
    final String reviewerLink =
        reviewerUrl
            + pullRequest.getAssociatedReviewerDiff()
            + "/"
            + pullRequest.getRepo()
            + "/"
            + reviewComment.getPath();

    ReviewComment githubComment =
        githubClient
            .createReviewComment(
                CreateReviewCommentRequest.newBuilder()
                    .setOwner(pullRequest.getOwner())
                    .setRepo(pullRequest.getRepo())
                    .setNumber(pullRequestNumber)
                    .setRequestData(
                        CreateReviewCommentRequest.CreateReviewCommentRequestData.newBuilder()
                            .setBody(getReviewCommentContent(reviewComment, reviewerLink))
                            .setCommitId(reviewComment.getCommitId())
                            .setPath(reviewComment.getPath())
                            .setPosition(reviewComment.getPosition())
                            .build())
                    .build())
            .getReviewComment();
    log.atInfo()
        .log(
            "Review comment with id *%s* was CREATED on GitHub(owner: %s, name: %s, PR number: %s): %s",
            githubComment.getId(),
            pullRequest.getOwner(),
            pullRequest.getRepo(),
            pullRequestNumber,
            reviewComment);
    return githubComment;
  }

  public IssueComment createIssueComment(
      String repoOwner,
      String repoName,
      long pullRequestNumber,
      IssueComment issueComment,
      long diffId) {
    final String reviewerLink = reviewerUrl + diffId;
    IssueComment comment =
        githubClient
            .createIssueComment(
                CreateIssueCommentRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setNumber(pullRequestNumber)
                    .setRequestData(
                        CreateIssueCommentRequest.CreateIssueCommentRequestData.newBuilder()
                            .setBody(
                                addReviewerBotInfo(
                                    issueComment.getUser().getEmail(),
                                    issueComment.getCreatedAt(),
                                    issueComment.getBody(),
                                    reviewerLink))
                            .build())
                    .build())
            .getIssueComment();
    log.atInfo()
        .log(
            "Issue comment with id *%s* was CREATED on GitHub(owner: %s, name: %s, PR number: %s): %s",
            comment.getId(), repoOwner, repoName, pullRequestNumber, issueComment);
    return comment;
  }

  public void editIssueComment(
      String repoOwner,
      String repoName,
      long commentId,
      String newBody,
      String author,
      String createdAt,
      long diffId) {
    final String reviewerLink = reviewerUrl + diffId;
    githubClient.editIssueComment(
        EditIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .setRequestData(
                EditIssueCommentRequest.EditIssueCommentRequestData.newBuilder()
                    .setBody(addReviewerBotInfo(author, createdAt, newBody, reviewerLink))
                    .build())
            .build());
    log.atInfo()
        .log(
            "Issue comment with id *%s* was EDITED on GitHub(owner: %s, name: %s). New content: %s",
            commentId, repoOwner, repoName, newBody);
  }

  public void deleteIssueComment(String repoOwner, String repoName, long commentId) {
    githubClient.deleteIssueComment(
        DeleteIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .build());
    log.atInfo()
        .log(
            "Issue comment with id *%s* was DELETED on GitHub(owner: %s, name: %s)",
            commentId, repoOwner, repoName);
  }

  public void editReviewComment(
      long diffNumber,
      String repoOwner,
      String repoName,
      long commentId,
      ReviewComment priorityComment) {
    final String reviewerLink =
        reviewerUrl + diffNumber + "/" + repoName + "/" + priorityComment.getPath();
    githubClient.editReviewComment(
        EditReviewCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .setRequestData(
                EditReviewCommentRequest.EditReviewCommentRequestData.newBuilder()
                    .setBody(getReviewCommentContent(priorityComment, reviewerLink))
                    .build())
            .build());
    log.atInfo()
        .log(
            "Review comment with id *%s* was EDITED on GitHub(owner: %s, name: %s). New content: %s",
            commentId, repoOwner, repoName, priorityComment.getBody());
  }

  public void deleteReviewComment(String repoOwner, String repoName, long commentId) {
    githubClient.deleteReviewComment(
        DeleteReviewCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .build());
    log.atInfo()
        .log(
            "Review comment with id *%s* was DELETED on GitHub(owner: %s, name: %s)",
            commentId, repoOwner, repoName);
  }

  private String getReviewCommentContent(ReviewComment reviewComment, String reviewerLink) {
    String result =
        addReviewerBotInfo(
            reviewComment.getUser().getEmail(),
            reviewComment.getCreatedAt(),
            reviewComment.getBody(),
            reviewerLink);
    if (reviewComment.getIsOutsideDiffComment()) {
      return "Synced from line: " + reviewComment.getReviewerLineNumber() + "\n" + result;
    } else {
      return result;
    }
  }

  private String addReviewerBotInfo(
      String author, String createdAt, String commentBody, String reviewerLink) {
    return "Author: "
        + author
        + "\nCreated time: "
        + createdAt
        + "\nBody: "
        + commentBody
        + "\nSee in Reviewer: "
        + reviewerLink;
  }
}

