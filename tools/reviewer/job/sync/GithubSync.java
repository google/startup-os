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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// TODO: Think about how to solve this issue:
/* In a situation where a comment was created on GitHub and then removed using Reviewer,
we do not know what exactly it is:
1) the comment was deleted by Reviewer or
2) a new comment created using GitHub.
*/
public class GithubSync {
  private ReviewerClient reviewerClient;
  private GithubWriter githubWriter;
  private GithubReader githubReader;

  public GithubSync(GithubClient githubClient, String reviewerUrl) {
    reviewerClient = new ReviewerClient();
    githubWriter = new GithubWriter(githubClient, reviewerUrl);
    githubReader = new GithubReader(githubClient);
  }

  public void syncWithGithub(long diffNumber, ImmutableMap<String, GitRepo> repoNameToGitRepos)
      throws IOException {
    Diff diff = reviewerClient.getDiff(diffNumber);
    ImmutableList<PullRequest> reviewerPullRequests =
        new DiffConverter(reviewerClient).toPullRequests(diff, repoNameToGitRepos);

    for (PullRequest reviewerPullRequest : reviewerPullRequests) {
      boolean isFirstSync = reviewerPullRequest.getNumber() == 0;
      if (isFirstSync) {
        long githubPullRequestNumber = createPullRequest(reviewerPullRequest, diffNumber);

        for (ReviewComment reviewComment : reviewerPullRequest.getReviewCommentList()) {
          createReviewCommentOnGithub(
              reviewerPullRequest, githubPullRequestNumber, reviewComment, diffNumber);
        }

        for (IssueComment issueComment : reviewerPullRequest.getIssueCommentList()) {
          createIssueCommentOnGithub(
              reviewerPullRequest, githubPullRequestNumber, issueComment, diffNumber);
        }
      } else {
        PullRequest githubPullRequest =
            githubReader.getPullRequest(
                reviewerPullRequest.getOwner(),
                reviewerPullRequest.getRepo(),
                reviewerPullRequest.getNumber());

        ImmutableList<ReviewCommentCorrelation> reviewCommentCorrelations =
            getReviewCommentCorrelations(
                reviewerPullRequest.getReviewCommentList(),
                githubPullRequest.getReviewCommentList());
        syncReviewComments(
            diffNumber, reviewerPullRequest, githubPullRequest, reviewCommentCorrelations);

        ImmutableList<IssueCommentCorrelation> issueCommentCorrelations =
            getIssueCommentCorrelations(
                reviewerPullRequest.getIssueCommentList(), githubPullRequest.getIssueCommentList());
        syncIssueComments(
            diffNumber, reviewerPullRequest, githubPullRequest, issueCommentCorrelations);
      }
    }
  }

  private long createPullRequest(PullRequest pullRequest, long diffNumber) {
    long githubPullRequestNumber =
        githubWriter.createPullRequest(pullRequest, pullRequest.getOwner(), pullRequest.getRepo());
    reviewerClient.addGithubPrNumber(
        diffNumber, pullRequest.getOwner(), pullRequest.getRepo(), githubPullRequestNumber);
    return githubPullRequestNumber;
  }

  private void createReviewCommentOnGithub(
      PullRequest pullRequest,
      long githubPullRequestNumber,
      ReviewComment reviewComment,
      long diffNumber) {
    ReviewComment githubComment =
        githubWriter.createReviewComment(githubPullRequestNumber, reviewComment, pullRequest);
    reviewerClient.addGithubReviewCommentId(
        diffNumber,
        reviewComment.getReviewerThreadId(),
        githubComment.getId(),
        reviewComment.getReviewerCommentId());
    reviewerClient.addGithubReviewCommentPosition(
        diffNumber,
        reviewComment.getReviewerThreadId(),
        githubComment.getPosition(),
        reviewComment.getReviewerCommentId());
  }

  private void createIssueCommentOnGithub(
      PullRequest reviewerPullRequest,
      long githubPullRequestNumber,
      IssueComment issueComment,
      long diffNumber) {
    githubWriter.createIssueComment(
        reviewerPullRequest.getOwner(),
        reviewerPullRequest.getRepo(),
        githubPullRequestNumber,
        issueComment,
        diffNumber);
    reviewerClient.addGithubIssueCommentId(
        diffNumber,
        issueComment.getReviewerThreadId(),
        issueComment.getId(),
        issueComment.getReviewerCommentId());
  }

  private ImmutableList<ReviewCommentCorrelation> getReviewCommentCorrelations(
      List<ReviewComment> reviewerReviewComments, List<ReviewComment> githubReviewComments) {
    List<ReviewCommentCorrelation> result = new ArrayList<>();
    for (ReviewComment reviewerComment : reviewerReviewComments) {
      ReviewCommentCorrelation reviewCommentCorrelation = new ReviewCommentCorrelation();
      reviewCommentCorrelation.reviewerComment = reviewerComment;
      reviewCommentCorrelation.githubComment =
          getAssociatedComment(githubReviewComments, reviewerComment);
      result.add(reviewCommentCorrelation);
    }

    for (ReviewComment githubComment : githubReviewComments) {
      int existingCommentCorrelationIndex = -1;
      for (ReviewCommentCorrelation correlation : result) {
        if (correlation.reviewerComment.getId() == githubComment.getId()) {
          existingCommentCorrelationIndex = result.indexOf(correlation);
        }
      }

      if (existingCommentCorrelationIndex == -1) {
        ReviewCommentCorrelation reviewCommentCorrelation = new ReviewCommentCorrelation();
        reviewCommentCorrelation.reviewerComment =
            getAssociatedComment(reviewerReviewComments, githubComment);
        reviewCommentCorrelation.githubComment = githubComment;
        result.add(reviewCommentCorrelation);
      } else {
        result.get(existingCommentCorrelationIndex).githubComment = githubComment;
      }
    }
    return ImmutableList.copyOf(result);
  }

  private ReviewComment getAssociatedComment(List<ReviewComment> comments, ReviewComment comment) {
    for (ReviewComment reviewComment : comments) {
      if (reviewComment.getId() == comment.getId()) {
        return reviewComment;
      }
    }
    return ReviewComment.getDefaultInstance();
  }

  private IssueComment getAssociatedComment(List<IssueComment> comments, IssueComment comment) {
    for (IssueComment issueComment : comments) {
      if (issueComment.getId() == comment.getId()) {
        return issueComment;
      }
    }
    return IssueComment.getDefaultInstance();
  }

  private ImmutableList<IssueCommentCorrelation> getIssueCommentCorrelations(
      List<IssueComment> reviewerIssueComments, List<IssueComment> githubIssueComments) {
    List<IssueCommentCorrelation> result = new ArrayList<>();
    for (IssueComment reviewerComment : reviewerIssueComments) {
      IssueCommentCorrelation issueCommentCorrelation = new IssueCommentCorrelation();
      issueCommentCorrelation.reviewerComment = reviewerComment;
      issueCommentCorrelation.githubComment =
          getAssociatedComment(githubIssueComments, reviewerComment);
      result.add(issueCommentCorrelation);
    }

    for (IssueComment githubComment : githubIssueComments) {
      int existingCommentCorrelationIndex = -1;
      for (IssueCommentCorrelation correlation : result) {
        if (correlation.reviewerComment.getId() == githubComment.getId()) {
          existingCommentCorrelationIndex = result.indexOf(correlation);
        }
      }

      if (existingCommentCorrelationIndex == -1) {
        IssueCommentCorrelation issueCommentCorrelation = new IssueCommentCorrelation();
        issueCommentCorrelation.reviewerComment =
            getAssociatedComment(reviewerIssueComments, githubComment);
        issueCommentCorrelation.githubComment = githubComment;
        result.add(issueCommentCorrelation);
      } else {
        result.get(existingCommentCorrelationIndex).githubComment = githubComment;
      }
    }
    return ImmutableList.copyOf(result);
  }

  private void syncReviewComments(
      long diffNumber,
      PullRequest reviewerPullRequest,
      PullRequest githubPullRequest,
      ImmutableList<ReviewCommentCorrelation> reviewCommentCorrelations) {
    for (ReviewCommentCorrelation reviewCommentCorrelation : reviewCommentCorrelations) {
      ReviewComment githubComment = reviewCommentCorrelation.githubComment;
      ReviewComment reviewerComment = reviewCommentCorrelation.reviewerComment;

      if (reviewerComment.getCreatedAt().isEmpty() && !githubComment.getCreatedAt().isEmpty()) {
        // The comment was deleted by Reviewer
        if (isCreatedByReviewerBot(githubComment.getBody())) {
          githubWriter.deleteReviewComment(
              githubPullRequest.getOwner(), githubPullRequest.getRepo(), githubComment.getId());
        }
        // The comment was created by GitHub
        reviewerClient.addCodeComment(diffNumber, githubComment);
        continue;
      }
      if (githubComment.getCreatedAt().isEmpty() && !reviewerComment.getCreatedAt().isEmpty()) {
        // The comment was created by Reviewer
        if (reviewerComment.getId() == 0) {
          createReviewCommentOnGithub(
              reviewerPullRequest,
              githubPullRequest.getNumber(),
              reviewCommentCorrelation.reviewerComment,
              diffNumber);
        } else {
          // The comment was deleted by GitHub
          reviewerClient.deleteCodeComment(
              diffNumber,
              reviewerComment.getReviewerThreadId(),
              reviewerComment.getReviewerCommentId(),
              reviewerComment.getId());
        }
        continue;
      }
      // The comment exists on Reviewer and GitHub
      updateExistingReviewComments(
          githubPullRequest.getOwner(),
          githubPullRequest.getRepo(),
          diffNumber,
          reviewerComment,
          githubComment);
    }
  }

  private void updateExistingReviewComments(
      String owner,
      String repoName,
      long diffNumber,
      ReviewComment reviewerComment,
      ReviewComment githubComment) {
    String githubCommentBody =
        isCreatedByReviewerBot(githubComment.getBody())
            ? getOnlyCommentBody(githubComment.getBody())
            : githubComment.getBody();
    if (!reviewerComment.getBody().equals(githubCommentBody)) {
      long reviewerCommentUpdatingTimestamp =
          Instant.parse(
                  reviewerComment.getUpdatedAt().equals("")
                      ? reviewerComment.getCreatedAt()
                      : reviewerComment.getUpdatedAt())
              .toEpochMilli();
      long githubCommentUpdatingTimestamp;
      if (isCreatedByReviewerBot(reviewerComment.getBody())) {
        githubCommentUpdatingTimestamp = getOriginalCreatedTimestamp(reviewerComment.getBody());
      } else {
        githubCommentUpdatingTimestamp = Instant.parse(githubComment.getUpdatedAt()).toEpochMilli();
      }

      if (reviewerCommentUpdatingTimestamp < githubCommentUpdatingTimestamp) {
        reviewerClient.updateCodeComment(
            diffNumber,
            reviewerComment.getReviewerThreadId(),
            reviewerComment.getReviewerCommentId(),
            reviewerComment.getId(),
            githubCommentBody);
      } else if (reviewerCommentUpdatingTimestamp > githubCommentUpdatingTimestamp) {
        githubWriter.editReviewComment(
            diffNumber, owner, repoName, githubComment.getId(), reviewerComment);
      }
    }
  }

  private void syncIssueComments(
      long diffNumber,
      PullRequest reviewerPullRequest,
      PullRequest githubPullRequest,
      ImmutableList<IssueCommentCorrelation> issueCommentCorrelations) {
    for (IssueCommentCorrelation issueCommentCorrelation : issueCommentCorrelations) {
      IssueComment githubComment = issueCommentCorrelation.githubComment;
      IssueComment reviewerComment = issueCommentCorrelation.reviewerComment;

      if (reviewerComment.getCreatedAt().isEmpty() && !githubComment.getCreatedAt().isEmpty()) {
        // The comment was deleted by Reviewer
        if (isCreatedByReviewerBot(githubComment.getBody())) {
          githubWriter.deleteIssueComment(
              githubPullRequest.getOwner(), githubPullRequest.getRepo(), githubComment.getId());
        }
        // The comment was created by GitHub
        reviewerClient.addDiffComment(diffNumber, githubComment);
        continue;
      }
      if (githubComment.getCreatedAt().isEmpty() && !reviewerComment.getCreatedAt().isEmpty()) {
        // The comment was created by Reviewer
        if (reviewerComment.getId() == 0) {
          createIssueCommentOnGithub(
              reviewerPullRequest,
              githubPullRequest.getNumber(),
              issueCommentCorrelation.reviewerComment,
              diffNumber);
        } else {
          // The comment was deleted by GitHub
          reviewerClient.deleteThreadComment(
              diffNumber,
              reviewerComment.getReviewerThreadId(),
              reviewerComment.getReviewerCommentId(),
              reviewerComment.getId());
        }
        continue;
      }
      // The comment exists on Reviewer and GitHub
      updateExistingIssueComments(
          githubPullRequest.getOwner(),
          githubPullRequest.getRepo(),
          diffNumber,
          reviewerComment,
          githubComment);
    }
  }

  private void updateExistingIssueComments(
      String owner,
      String repoName,
      long diffNumber,
      IssueComment reviewerComment,
      IssueComment githubComment) {
    String githubCommentBody =
        isCreatedByReviewerBot(githubComment.getBody())
            ? getOnlyCommentBody(githubComment.getBody())
            : githubComment.getBody();
    if (!reviewerComment.getBody().equals(githubCommentBody)) {
      long reviewerCommentUpdatingTimestamp =
          Instant.parse(
                  reviewerComment.getUpdatedAt().equals("")
                      ? reviewerComment.getCreatedAt()
                      : reviewerComment.getUpdatedAt())
              .toEpochMilli();
      long githubCommentUpdatingTimestamp;
      if (isCreatedByReviewerBot(reviewerComment.getBody())) {
        githubCommentUpdatingTimestamp = getOriginalCreatedTimestamp(reviewerComment.getBody());
      } else {
        githubCommentUpdatingTimestamp = Instant.parse(githubComment.getUpdatedAt()).toEpochMilli();
      }

      if (reviewerCommentUpdatingTimestamp < githubCommentUpdatingTimestamp) {
        reviewerClient.updateDiffComment(
            diffNumber,
            reviewerComment.getReviewerThreadId(),
            reviewerComment.getReviewerCommentId(),
            reviewerComment.getId(),
            githubCommentBody);
      } else if (reviewerCommentUpdatingTimestamp > githubCommentUpdatingTimestamp) {
        githubWriter.editIssueComment(
            owner,
            repoName,
            githubComment.getId(),
            reviewerComment.getBody(),
            reviewerComment.getUser().getEmail(),
            reviewerComment.getCreatedAt(),
            diffNumber);
      }
    }
  }

  private static boolean isCreatedByReviewerBot(String commentBody) {
    return commentBody.contains("See in Reviewer");
  }

  private static long getOriginalCreatedTimestamp(String comment) {
    String createdAt = getSubstringBetweenTwoStrings(comment, "Created time: ", "Body: ");
    return Instant.parse(createdAt).toEpochMilli();
  }

  private static String getOnlyCommentBody(String comment) {
    return getSubstringBetweenTwoStrings(comment, "Body: ", "See in Reviewer:");
  }

  private static String getSubstringBetweenTwoStrings(String text, String before, String after) {
    String result = text.substring(text.indexOf(before) + before.length(), text.length());
    return result.substring(0, result.indexOf(after)).trim();
  }

  private class ReviewCommentCorrelation {
    private ReviewComment reviewerComment;
    private ReviewComment githubComment;
  }

  private class IssueCommentCorrelation {
    private IssueComment reviewerComment;
    private IssueComment githubComment;
  }
}

