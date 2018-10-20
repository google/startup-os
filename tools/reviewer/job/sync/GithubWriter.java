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

import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestReviewRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestReviewRequestData;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequestData;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.Review;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;

/**
 * Writes `tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest` to GitHub using GitHub API
 */
public class GithubWriter {
  private final GithubClient githubClient;

  GithubWriter(GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  public void writePullRequest(PullRequest pullRequest, String repoOwner, String repoName) {
    long pullRequestNumber =
        pullRequest.getNumber() == 0
            ? createPullRequest(pullRequest, repoOwner, repoName)
            : pullRequest.getNumber();

    for (Review review : pullRequest.getReviewsList()) {
      createReview(repoOwner, repoName, review, pullRequestNumber);
    }

    for (IssueComment issueComment : pullRequest.getIssueCommentList()) {
      createIssueComment(repoOwner, repoName, pullRequestNumber, issueComment);
    }
  }

  private void createIssueComment(
      String repoOwner, String repoName, long pullRequestNumber, IssueComment issueComment) {
    githubClient.createIssueComment(
        GithubProtos.CreateIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setNumber(pullRequestNumber)
            .setRequestData(
                GithubProtos.CreateIssueCommentRequestData.newBuilder()
                    // TODO: Add additional info to text message and link to Reviewer
                    .setBody(issueComment.getBody())
                    .build())
            .build());
  }

  private void createReview(
      String repoOwner, String repoName, Review review, long pullRequestNumber) {
    CreatePullRequestReviewRequestData.Builder requestDataBuilder =
        CreatePullRequestReviewRequestData.newBuilder();
    requestDataBuilder
        .setCommitId(review.getCommitId())
        .setBody(review.getBody())
        .setEvent(setEvent(review.getState()));

    review
        .getReviewCommentList()
        .forEach(
            reviewComment ->
                requestDataBuilder.addComments(
                    GithubProtos.ReviewCommentRequestData.newBuilder()
                        .setPath(reviewComment.getPath())
                        .setPosition(reviewComment.getPosition())
                        // TODO: Add additional info to text message and link to Reviewer
                        .setBody(reviewComment.getBody())
                        .build()));

    githubClient.createPullRequestReview(
        CreatePullRequestReviewRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setNumber(pullRequestNumber)
            .setRequestData(requestDataBuilder.build())
            .build());
  }

  private long createPullRequest(PullRequest pullRequest, String repoOwner, String repoName) {
    return githubClient
        .createPullRequest(
            CreatePullRequestRequest.newBuilder()
                .setOwner(repoOwner)
                .setRepo(repoName)
                .setRequestData(
                    CreatePullRequestRequestData.newBuilder()
                        .setTitle(pullRequest.getTitle())
                        .setHead(pullRequest.getHead().getRef())
                        .setBase(pullRequest.getBase().getRef())
                        // Body can't be empty
                        .setBody(
                            pullRequest.getBody().isEmpty()
                                ? "Created by ReviewerBot"
                                : pullRequest.getBody())
                        .build())
                .build())
        .getPullRequest()
        .getNumber();
  }

  private String setEvent(Review.State state) {
    if (state.equals(Review.State.APPROVED)) {
      return "APPROVE";
    } else if (state.equals(Review.State.CHANGES_REQUESTED)) {
      return "REQUEST_CHANGES";
    } else if (state.equals(Review.State.COMMENTED)) {
      return "COMMENT";
    } else {
      return "PENDING";
    }
  }
}

