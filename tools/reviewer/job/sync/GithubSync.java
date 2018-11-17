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

import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GithubSync {
  // TODO: Add checking input Flags
  @FlagDesc(name = "login", description = "GitHub login")
  private static Flag<String> login = Flag.create("");

  @FlagDesc(name = "password", description = "GitHub password")
  private static Flag<String> password = Flag.create("");

  @FlagDesc(name = "reviewer_diff_link", description = "Base url to reviewer diff")
  private static Flag<String> reviewerDiffLink =
      Flag.create("https://startupos-5f279.firebaseapp.com/diff/");

  @FlagDesc(name = "diffs_to_sync", description = "List of Reviewer Diff number to sync")
  private static final Flag<List<Long>> diffsToSync =
      Flag.createLongsListFlag(Collections.emptyList());

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    GithubClient githubClient = new GithubClient(login.get(), password.get());

    // Go over all Reviewer Diff numbers to sync
    for (long diffNumber : diffsToSync.get()) {
      syncWithGithub(githubClient, diffNumber);
    }
  }

  private static void syncWithGithub(GithubClient githubClient, long diffNumber)
      throws IOException {
    DiffWriter diffWriter = new DiffWriter();
    DiffReader diffReader = new DiffReader();
    GithubWriter githubWriter = new GithubWriter(githubClient, reviewerDiffLink.get());
    GithubReader githubReader = new GithubReader(githubClient);

    // Get Diff from Firebase
    Diff diff = diffReader.getDiff(diffNumber);
    // Convert Diff to GitHub PullRequests
    List<PullRequest> reviewerPullRequests =
        new DiffConverter(githubClient).convertDiffToPullRequests(diff);

    // Go over all GitHub PullRequests
    for (PullRequest reviewerPullRequest : reviewerPullRequests) {

      // `reviewerPullRequest.getNumber() == 0` means that it's the first sync.
      // GitHub doesn't have Pull Request
      if (reviewerPullRequest.getNumber() == 0) {
        // Create GitHub Pull Request
        long githubPullRequestNumber =
            githubWriter.createPullRequest(
                reviewerPullRequest, reviewerPullRequest.getOwner(), reviewerPullRequest.getRepo());
        diffWriter.addGithubPrNumber(
            diffNumber,
            reviewerPullRequest.getOwner(),
            reviewerPullRequest.getRepo(),
            githubPullRequestNumber);

        if (!reviewerPullRequest.getReviewCommentList().isEmpty()) {
          for (ReviewComment reviewComment : reviewerPullRequest.getReviewCommentList()) {
            // Create GitHub Review comment
            long githubReviewCommentId =
                githubWriter.createReviewComment(
                    reviewerPullRequest.getOwner(),
                    reviewerPullRequest.getRepo(),
                    githubPullRequestNumber,
                    reviewComment,
                    reviewerPullRequest);
            diffWriter.addGithubReviewCommentId(
                diffNumber,
                reviewComment.getReviewerThreadId(),
                githubReviewCommentId,
                reviewComment.getUser().getEmail(),
                reviewComment.getCreatedAt());
          }
        }
        if (!reviewerPullRequest.getIssueCommentList().isEmpty()) {
          for (IssueComment issueComment : reviewerPullRequest.getIssueCommentList()) {
            // Create GitHub Issue comment
            long githubIssueCommentId =
                githubWriter.createIssueComment(
                    reviewerPullRequest.getOwner(),
                    reviewerPullRequest.getRepo(),
                    githubPullRequestNumber,
                    issueComment,
                    reviewerPullRequest.getAssociatedReviewerDiff());
            diffWriter.addGithubIssueCommentId(
                diffNumber,
                issueComment.getReviewerThreadId(),
                githubIssueCommentId,
                issueComment.getUser().getEmail(),
                issueComment.getCreatedAt());
          }
        }
        // It isn't the first sync. GitHub Pull Request is already exist
      } else {
        // GitHub's PullRequests
        PullRequest githubPullRequest =
            githubReader.getPullRequest(
                reviewerPullRequest.getOwner(),
                reviewerPullRequest.getRepo(),
                reviewerPullRequest.getNumber());
        // GitHub review comment id to GitHub review comment
        Map<Long, ReviewComment> githubReviewCommentIdToGithubReviewComment =
            githubPullRequest
                .getReviewCommentList()
                .stream()
                .collect(Collectors.toMap(ReviewComment::getId, comment -> comment));

        // Go over all Reviewer's PullRequests
        for (ReviewComment reviewComment : reviewerPullRequest.getReviewCommentList()) {
          // `reviewerComment.getId() == 0` means that the current comment doesn't exist in GitHub
          if (reviewComment.getId() == 0) {
            // Create review comment in GitHub
            long githubReviewCommentId =
                githubWriter.createReviewComment(
                    reviewerPullRequest.getOwner(),
                    reviewerPullRequest.getRepo(),
                    reviewerPullRequest.getNumber(),
                    reviewComment,
                    reviewerPullRequest);
            diffWriter.addGithubReviewCommentId(
                diffNumber,
                reviewComment.getReviewerThreadId(),
                githubReviewCommentId,
                reviewComment.getUser().getEmail(),
                reviewComment.getCreatedAt());
            // The current comment exists in GitHub
          } else {
            // Comment updating time in Reviewer
            long reviewerCommentUpdating =
                Instant.parse(
                        reviewComment.getUpdatedAt().equals("")
                            ? reviewComment.getCreatedAt()
                            : reviewComment.getUpdatedAt())
                    .toEpochMilli();
            // Comment updating time in GitHub
            long githubCommentUpdating;
            if (isCreatedInReviewer(reviewComment.getBody())) {
              githubCommentUpdating = getOriginalCreatedTimestamp(reviewComment.getBody());
            } else {
              githubCommentUpdating =
                  Instant.parse(
                          githubReviewCommentIdToGithubReviewComment
                              .get(reviewComment.getId())
                              .getUpdatedAt())
                      .toEpochMilli();
            }

            // the comment in GitHub has a priority
            if (reviewerCommentUpdating < githubCommentUpdating) {
              // TODO: Compare comment body before updating. If they are the same don't update.
              // Also below
              diffWriter.updateCodeComment(
                  diffNumber,
                  reviewComment.getReviewerThreadId(),
                  reviewComment.getId(),
                  githubReviewCommentIdToGithubReviewComment.get(reviewComment.getId()).getBody());
              // delete from map element which already is sync
              githubReviewCommentIdToGithubReviewComment.remove(reviewComment.getId());
              // the comment in Reviewer has a priority
            } else if (reviewerCommentUpdating > githubCommentUpdating) {
              githubWriter.editReviewComment(
                  reviewerPullRequest.getOwner(),
                  reviewerPullRequest.getRepo(),
                  reviewerPullRequest.getNumber(),
                  reviewComment.getBody());
              // delete from map element which already is sync
              githubReviewCommentIdToGithubReviewComment.remove(reviewComment.getId());
            }
          }
        }
        // delete from GitHub comments which are not exist in Reviewer
        githubReviewCommentIdToGithubReviewComment.forEach(
            (id, comment) -> {
              if (isCreatedInReviewer(comment.getBody())) {
                githubWriter.deleteReviewComment(
                    reviewerPullRequest.getOwner(), reviewerPullRequest.getRepo(), comment.getId());
              } else {
                diffWriter.addCodeComment(diffNumber, comment);
              }
            });

        // GitHub issue comment id to GitHub issue comment
        Map<Long, IssueComment> githubIssueCommentIdToGithubIssueComment =
            githubPullRequest
                .getIssueCommentList()
                .stream()
                .collect(Collectors.toMap(IssueComment::getId, comment -> comment));

        // Go over all Reviewer's Issue Comments
        for (IssueComment issueComment : reviewerPullRequest.getIssueCommentList()) {
          // `issueComment.getId() == 0` means that the current comment doesn't exist in GitHub
          if (issueComment.getId() == 0) {
            // Create issue comment in GitHub
            long githubIssueCommentId =
                githubWriter.createIssueComment(
                    reviewerPullRequest.getOwner(),
                    reviewerPullRequest.getRepo(),
                    reviewerPullRequest.getNumber(),
                    issueComment,
                    reviewerPullRequest.getAssociatedReviewerDiff());
            diffWriter.addGithubIssueCommentId(
                diffNumber,
                issueComment.getReviewerThreadId(),
                githubIssueCommentId,
                issueComment.getUser().getEmail(),
                issueComment.getCreatedAt());
            // The current comment exists in GitHub
          } else {
            // Comment updating time in Reviewer
            long reviewerCommentUpdating = Long.parseLong(issueComment.getUpdatedAt());
            // Comment updating time in GitHub
            long githubCommentUpdating;
            if (isCreatedInReviewer(issueComment.getBody())) {
              githubCommentUpdating = getOriginalCreatedTimestamp(issueComment.getBody());
            } else {
              githubCommentUpdating =
                  Instant.parse(
                          githubIssueCommentIdToGithubIssueComment
                              .get(issueComment.getId())
                              .getUpdatedAt())
                      .toEpochMilli();
            }

            // the comment in GitHub has a priority
            if (reviewerCommentUpdating < githubCommentUpdating) {
              diffWriter.updateDiffComment(
                  diffNumber,
                  issueComment.getReviewerThreadId(),
                  issueComment.getId(),
                  githubIssueCommentIdToGithubIssueComment.get(issueComment.getId()).getBody());
              // the comment in Reviewer has a priority
            } else if (reviewerCommentUpdating > githubCommentUpdating) {
              githubWriter.editIssueComment(
                  reviewerPullRequest.getOwner(),
                  reviewerPullRequest.getRepo(),
                  reviewerPullRequest.getNumber(),
                  issueComment.getBody());
            }
            // delete from map element which already is sync
            githubIssueCommentIdToGithubIssueComment.remove(issueComment.getId());
          }
          // delete from GitHub comments which are not exist in Reviewer
          githubIssueCommentIdToGithubIssueComment.forEach(
              (id, comment) -> {
                if (isCreatedInReviewer(comment.getBody())) {
                  githubWriter.deleteIssueComment(
                      reviewerPullRequest.getOwner(),
                      reviewerPullRequest.getRepo(),
                      comment.getId());
                } else {
                  diffWriter.addDiffComment(diffNumber, comment);
                }
              });
        }
      }
    }
  }

  private static boolean isCreatedInReviewer(String commentBody) {
    return commentBody.contains("See in Reviewer");
  }

  private static long getOriginalCreatedTimestamp(String body) {
    String createdAt = getSubstringBetweenTwoStrings(body, "Created time: ", "Body: ");
    return Instant.parse(createdAt).toEpochMilli();
  }

  private static String getSubstringBetweenTwoStrings(String text, String before, String after) {
    String result = text.substring(text.indexOf(before) + before.length(), text.length());
    return result.substring(0, result.indexOf(after)).trim();
  }
}

