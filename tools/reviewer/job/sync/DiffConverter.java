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
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.User;
import com.google.startupos.tools.reviewer.localserver.service.Protos;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.GithubPr;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// Converts a Diff proto to a list of GithubPullRequestProtos.PullRequest protos
public class DiffConverter {

  public ImmutableList<PullRequest> toPullRequests(Diff diff, String patch) {
    ImmutableList.Builder<PullRequest> pullRequests = ImmutableList.builder();

    for (GithubPr githubPr : diff.getGithubPrList()) {
      PullRequest.Builder pullRequest = PullRequest.newBuilder();

      if (githubPr.getNumber() != 0) {
        pullRequest.setNumber(githubPr.getNumber());
      }
      pullRequest
          .setState(getPullRequestState(diff.getStatus()))
          .setTitle("D" + diff.getId())
          .setUser(User.newBuilder().setEmail(diff.getAuthor().getEmail()).build())
          .setBody(diff.getDescription())
          .setCreatedAt(Instant.ofEpochMilli(diff.getCreatedTimestamp()).toString())
          .setUpdatedAt(Instant.ofEpochMilli(diff.getModifiedTimestamp()).toString())
          .setBaseBranchName("master")
          .setHeadBranchName("D" + diff.getId())
          .setRepo(githubPr.getRepo())
          .addAllReviewComment(
              getReviewCommentsByRepoName(
                  diff.getCodeThreadList(),
                  githubPr.getRepo(),
                  diff.getBaseBranchCommitId(),
                  patch))
          .addAllIssueComment(getIssueComments(diff.getDiffThreadList(), githubPr.getRepo()))
          .setOwner(githubPr.getOwner())
          .setAssociatedReviewerDiff(diff.getId());
      pullRequests.add(pullRequest.build());
    }
    return pullRequests.build();
  }

  private ImmutableList<ReviewComment> getReviewCommentsByRepoName(
      List<Thread> codeThreads, String repoName, String baseBranchCommitId, String patch) {
    ImmutableList.Builder<ReviewComment> result = ImmutableList.builder();
    ImmutableList<Thread> codeThreadsByRepo =
        ImmutableList.copyOf(
            codeThreads
                .stream()
                .filter(thread -> thread.getRepoId().equals(repoName))
                .collect(Collectors.toList()));

    for (Thread thread : codeThreadsByRepo) {
      for (Comment comment : thread.getCommentList()) {
        int githubCommentPosition;
        // `0` value means the comment isn't synced with GitHub
        if (thread.getGithubCommentPosition() == 0) {
          githubCommentPosition =
              getReviewCommentPositionFromGitHub(baseBranchCommitId, thread, patch);
        } else {
          githubCommentPosition = thread.getGithubCommentPosition();
        }

        ReviewComment.Builder githubComment = ReviewComment.newBuilder();
        githubComment
            .setPath(thread.getFile().getFilename())
            .setId(comment.getGithubCommentId())
            .setPosition(githubCommentPosition)
            .setCommitId(thread.getFile().getCommitId())
            .setUser(User.newBuilder().setEmail(comment.getCreatedBy()).build())
            .setBody(comment.getContent())
            .setCreatedAt(String.valueOf(Instant.ofEpochMilli(comment.getTimestamp())))
            .setReviewerThreadId(thread.getId())
            .build();
        result.add(githubComment.build());
      }
    }
    return result.build();
  }

  private String getPullRequestState(Diff.Status status) {
    if (status.equals(Diff.Status.SUBMITTED) || status.equals(Diff.Status.REVERTED)) {
      return "close";
    } else {
      return "open";
    }
  }

  private int getReviewCommentPositionFromGitHub(
      String baseBranchCommitId, Thread thread, String patch) {
    LineNumberConverter.Side commentSide =
        baseBranchCommitId.equals(thread.getCommitId())
            ? LineNumberConverter.Side.LEFT
            : LineNumberConverter.Side.RIGHT;

    if (patch.equals("")) {
      throw new RuntimeException("Patch is empty for file: " + thread.getFile().getFilename());
    }
    // TODO: Store the result in the Diff.Thread in Firestore
    return LineNumberConverter.getPosition(patch, thread.getLineNumber(), commentSide);
  }

  private ImmutableList<IssueComment> getIssueComments(List<Thread> diffThreads, String repoName) {
    ImmutableList.Builder<IssueComment> result = ImmutableList.builder();
    ImmutableList<Thread> diffThreadsByRepo =
        ImmutableList.copyOf(
            diffThreads
                .stream()
                .filter(thread -> thread.getRepoId().equals(repoName))
                .collect(Collectors.toList()));
    for (Protos.Thread thread : diffThreadsByRepo) {
      thread
          .getCommentList()
          .forEach(
              comment ->
                  result.add(
                      IssueComment.newBuilder()
                          .setId(comment.getGithubCommentId())
                          .setBody(comment.getContent())
                          .setCreatedAt(Instant.ofEpochMilli(comment.getTimestamp()).toString())
                          .setUser(
                              GithubPullRequestProtos.User.newBuilder()
                                  .setEmail(comment.getCreatedBy())
                                  .build())
                          .build()));
    }
    return result.build();
  }
}

