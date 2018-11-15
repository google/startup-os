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
    return githubClient
        .createPullRequest(
            CreatePullRequestRequest.newBuilder()
                .setOwner(repoOwner)
                .setRepo(repoName)
                .setRequestData(
                    CreatePullRequestRequest.CreatePullRequestRequestData.newBuilder()
                        .setTitle(pullRequest.getTitle())
                        .setHead(pullRequest.getHead().getRef())
                        .setBase(pullRequest.getBase().getRef())
                        .setBody(pullRequest.getBody())
                        .build())
                .build())
        .getPullRequest()
        .getNumber();
  }

  public long createReviewComment(
      String repoOwner,
      String repoName,
      long pullRequestNumber,
      ReviewComment reviewComment,
      PullRequest pullRequest) {
    final String reviewerLink =
        reviewerUrl
            + pullRequest.getAssociatedReviewerDiff()
            + "/"
            + pullRequest.getRepo()
            + "/"
            + reviewComment.getPath();
    return githubClient
        .createReviewComment(
            CreateReviewCommentRequest.newBuilder()
                .setOwner(repoOwner)
                .setRepo(repoName)
                .setNumber(pullRequestNumber)
                .setRequestData(
                    CreateReviewCommentRequest.CreateReviewCommentRequestData.newBuilder()
                        .setBody(
                            "Author: "
                                + reviewComment.getUser().getEmail()
                                + "\nCreated time: "
                                + reviewComment.getCreatedAt()
                                + "\nBody: "
                                + reviewComment.getBody()
                                + "\nSee in Reviewer: "
                                + reviewerLink)
                        .setCommitId(reviewComment.getCommitId())
                        .setPath(reviewComment.getPath())
                        .setPosition(reviewComment.getPosition())
                        .build())
                .build())
        .getReviewComment()
        .getId();
  }

  public long createIssueComment(
      String repoOwner,
      String repoName,
      long pullRequestNumber,
      IssueComment issueComment,
      long diffId) {
    final String reviewerLink = reviewerUrl + diffId;
    return githubClient
        .createIssueComment(
            CreateIssueCommentRequest.newBuilder()
                .setOwner(repoOwner)
                .setRepo(repoName)
                .setNumber(pullRequestNumber)
                .setRequestData(
                    CreateIssueCommentRequest.CreateIssueCommentRequestData.newBuilder()
                        .setBody(
                            "Author: "
                                + issueComment.getUser().getEmail()
                                + "\nCreated time: "
                                + issueComment.getCreatedAt()
                                + "\nBody: "
                                + issueComment.getBody()
                                + "\nSee in Reviewer: "
                                + reviewerLink)
                        .build())
                .build())
        .getIssueComment()
        .getId();
  }

  // TODO: We can edit only own comments. Think over how to edit others comments.
  public void editIssueComment(String repoOwner, String repoName, long commentId, String newBody) {
    githubClient.editIssueComment(
        EditIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .setRequestData(
                EditIssueCommentRequest.EditIssueCommentRequestData.newBuilder()
                    .setBody(newBody)
                    .build())
            .build());
  }

  // TODO: We can delete only own comments. Think over how to edit others comments.
  public void deleteIssueComment(String repoOwner, String repoName, long commentId) {
    githubClient.deleteIssueComment(
        DeleteIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .build());
  }

  // TODO: We can edit only own comments. Think over how to edit others comments.
  public void editReviewComment(String repoOwner, String repoName, long commentId, String newBody) {
    githubClient.editReviewComment(
        EditReviewCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .setRequestData(
                EditReviewCommentRequest.EditReviewCommentRequestData.newBuilder()
                    .setBody(newBody)
                    .build())
            .build());
  }

  // TODO: We can delete only own comments. Think over how to edit others comments.
  public void deleteReviewComment(String repoOwner, String repoName, long commentId) {
    githubClient.deleteReviewComment(
        DeleteReviewCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setCommentId(commentId)
            .build());
  }
}

