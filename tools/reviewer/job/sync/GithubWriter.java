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
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.ReviewsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.IssueCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.Review;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes `tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest` to GitHub using GitHub API
 */
public class GithubWriter {
  private final GithubClient githubClient;

  GithubWriter(GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  /**
   * Creates GitHub PullRequest, GitHub Reviews and GitHub IssueComments from scratch
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
                            .setHead(pullRequest.getHead().getRef())
                            .setBase(pullRequest.getBase().getRef())
                            .setBody(pullRequest.getBody())
                            .build())
                    .build())
            .getPullRequest()
            .getNumber();

    pullRequest
        .getReviewsList()
        .forEach(review -> createReview(repoOwner, repoName, review, pullRequestNumber));
    pullRequest
        .getIssueCommentList()
        .forEach(
            issueComment ->
                createIssueComment(repoOwner, repoName, pullRequestNumber, issueComment));
    return pullRequestNumber;
  }

  /**
   * Updates GitHub PullRequest. Adds new GitHub Reviews and GitHub IssueComments to existing GitHub
   * PullRequest
   *
   * @param newPullRequest Proto message from which will be updated GitHub PullRequest
   * @param repoOwner Owner of GirHub repository
   * @param repoName Name GirHub repository
   */
  public void updatePullRequest(PullRequest newPullRequest, String repoOwner, String repoName)
      throws IOException {
    List<Review> alreadyExistingGithubReviews =
        githubClient
            .getReviews(
                ReviewsRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setNumber(newPullRequest.getNumber())
                    .build())
            .getReviewsList();
    List<Long> alreadyExistingGithubReviewIds =
        alreadyExistingGithubReviews.stream().map(Review::getId).collect(Collectors.toList());

    List<Review> reviewsToWrite = new LinkedList<>(newPullRequest.getReviewsList());
    reviewsToWrite.removeIf(review -> alreadyExistingGithubReviewIds.contains(review.getId()));
    reviewsToWrite.forEach(
        review -> createReview(repoOwner, repoName, review, newPullRequest.getNumber()));

    List<IssueComment> alreadyExistingGithubIssueComments =
        githubClient
            .getIssueComments(
                IssueCommentsRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setNumber(newPullRequest.getNumber())
                    .build())
            .getIssueCommentList();

    List<IssueComment> issueCommentsToWrite =
        new LinkedList<>(newPullRequest.getIssueCommentList());
    issueCommentsToWrite.removeIf(alreadyExistingGithubIssueComments::contains);
    issueCommentsToWrite.forEach(
        issueComment ->
            createIssueComment(repoOwner, repoName, newPullRequest.getNumber(), issueComment));
  }

  private void createIssueComment(
      String repoOwner, String repoName, long pullRequestNumber, IssueComment issueComment) {
    githubClient.createIssueComment(
        CreateIssueCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setNumber(pullRequestNumber)
            .setRequestData(
                CreateIssueCommentRequest.CreateIssueCommentRequestData.newBuilder()
                    // TODO: Add additional info to text message and link to Reviewer
                    .setBody(issueComment.getBody())
                    .build())
            .build());
  }

  private void createReview(
      String repoOwner, String repoName, Review review, long pullRequestNumber) {
    CreatePullRequestReviewRequest.CreatePullRequestReviewRequestData.Builder requestDataBuilder =
        CreatePullRequestReviewRequest.CreatePullRequestReviewRequestData.newBuilder();
    requestDataBuilder
        .setCommitId(review.getCommitId())
        .setBody(review.getBody())
        .setEvent(setEvent(review.getState()));

    review
        .getReviewCommentList()
        .forEach(
            reviewComment ->
                requestDataBuilder.addComments(
                    CreatePullRequestReviewRequest.CreatePullRequestReviewRequestData
                        .ReviewCommentRequestData.newBuilder()
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

