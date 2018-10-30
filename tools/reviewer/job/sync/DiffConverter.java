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

import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.CommitPointer;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.Review;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.User;
import com.google.startupos.tools.reviewer.localserver.service.Protos;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.GithubPr;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DiffConverter {

  private GithubClient githubClient;

  public DiffConverter(GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  private enum CommitPointerType {
    BASE,
    HEAD
  }

  public List<PullRequest> convertDiffToPullRequests(Diff diff) throws IOException {
    List<PullRequest> pullRequests = new ArrayList<>();
    for (GithubPr githubPr : diff.getGithubPrList()) {
      PullRequest.Builder pullRequest = PullRequest.newBuilder();

      pullRequest
          .setNumber(githubPr.getNumber())
          .setState(getPullRequestState(diff.getStatus()))
          .setTitle("D" + diff.getId())
          .setUser(User.newBuilder().setEmail(diff.getAuthor().getEmail()).build())
          .setBody(diff.getDescription())
          .setCreatedAt(Instant.ofEpochMilli(diff.getCreatedTimestamp()).toString())
          .setUpdatedAt(Instant.ofEpochMilli(diff.getModifiedTimestamp()).toString())
          .addAllReviews(
              getReviews(
                  diff.getCodeThreadList()
                      .stream()
                      .filter(thread -> thread.getRepoId().equals(githubPr.getRepo().getRepo()))
                      .collect(Collectors.toList()),
                  diff.getStatus(),
                  githubPr))
          .addAllIssueComment(getIssueComments(diff.getDiffThreadList()))
          .setBase(getCommitPointer(githubPr, CommitPointerType.BASE))
          .setHead(getCommitPointer(githubPr, CommitPointerType.HEAD))
          .setRepo(githubPr.getRepo().getRepo())
          .addAllCommitsInfo(getCommits(githubPr));
      pullRequests.add(pullRequest.build());
    }
    return pullRequests;
  }

  // TODO: Check if the state is correct
  private String getPullRequestState(Diff.Status status) {
    if (status.equals(Diff.Status.ACCEPTED)) {
      return "close";
    } else {
      return "open";
    }
  }

  private CommitPointer getCommitPointer(GithubPr githubPr, CommitPointerType commitPointerType)
      throws IOException {
    // We suppose GitHub Pull Request already exists in GitHub
    PullRequest existingPullRequest =
        githubClient
            .getPullRequest(
                GithubProtos.PullRequestRequest.newBuilder()
                    .setOwner(githubPr.getRepo().getOwner())
                    .setRepo(githubPr.getRepo().getRepo())
                    .setNumber(githubPr.getNumber())
                    .build())
            .getPullRequest();

    if (commitPointerType.equals(CommitPointerType.BASE)) {
      return existingPullRequest.getBase();
    } else {
      return existingPullRequest.getHead();
    }
  }

  private List<GithubPullRequestProtos.CommitInfo> getCommits(GithubPr githubPr)
      throws IOException {
    List<GithubPullRequestProtos.CommitInfo> pullRequestCommits =
        githubClient
            .getCommits(
                GithubProtos.CommitsRequest.newBuilder()
                    .setOwner(githubPr.getRepo().getOwner())
                    .setRepo(githubPr.getRepo().getRepo())
                    .setNumber(githubPr.getNumber())
                    .build())
            .getCommitsList();
    /* `https://developer.github.com/v3/pulls/#list-commits-on-a-pull-request` request doesn't provide
    files information. We send additional requests to get it. */
    List<GithubPullRequestProtos.CommitInfo> result = new ArrayList<>();
    for (GithubPullRequestProtos.CommitInfo commit : pullRequestCommits) {
      result.add(
          githubClient
              .getCommit(
                  GithubProtos.CommitRequest.newBuilder()
                      .setOwner(githubPr.getRepo().getOwner())
                      .setRepo(githubPr.getRepo().getRepo())
                      .setSha(commit.getSha())
                      .build())
              .getCommit());
    }
    return result;
  }

  private List<Review> getReviews(List<Thread> codeThreads, Diff.Status status, GithubPr githubPr) {
    List<Review> result = new ArrayList<>();

    Set<String> commitIds = new HashSet<>();
    codeThreads.forEach(thread -> commitIds.add(thread.getFile().getCommitId()));

    for (String commitId : commitIds) {
      List<Thread> threadsByCommit =
          codeThreads
              .stream()
              .filter(thread -> commitId.equals(thread.getFile().getCommitId()))
              .collect(Collectors.toList());

      Set<String> authors = new HashSet<>();
      threadsByCommit.forEach(
          thread ->
              thread.getCommentList().forEach(comment -> authors.add(comment.getCreatedBy())));

      Map<String, Map<Thread, Comment>> authorToThreadToComment = new HashMap<>();
      for (String author : authors) {
        Map<Thread, Comment> threadToComment = new HashMap<>();
        for (Thread thread : threadsByCommit) {
          for (Comment comment : thread.getCommentList()) {
            if (comment.getCreatedBy().equals(author)) {
              threadToComment.put(thread, comment);
            }
          }
          authorToThreadToComment.put(author, threadToComment);
        }
      }

      authorToThreadToComment.forEach(
          (author, comments) ->
              result.add(
                  Review.newBuilder()
                      .setUser(User.newBuilder().setEmail(author).build())
                      .setBody("Created by Reviewer")
                      .setCommitId(commitId)
                      .setState(getReviewState(status))
                      .addAllReviewComment(getReviewComments(comments, githubPr))
                      .build()));
    }
    return result;
  }

  // TODO: Check if the state is correct
  private Review.State getReviewState(Diff.Status status) {
    if (status.equals(Diff.Status.ACCEPTED)) {
      return Review.State.APPROVED;
    }
    if (status.equals(Diff.Status.NEEDS_MORE_WORK)) {
      return Review.State.CHANGES_REQUESTED;
    }
    if (status.equals(Diff.Status.UNDER_REVIEW)) {
      return Review.State.COMMENTED;
    }
    return Review.State.UNKNOWN;
  }

  private List<ReviewComment> getReviewComments(Map<Thread, Comment> comments, GithubPr githubPr) {
    List<ReviewComment> reviewComments = new ArrayList<>();

    comments.forEach(
        ((thread, comment) ->
            reviewComments.add(
                ReviewComment.newBuilder()
                    .setPath(thread.getFile().getFilename())
                    .setCommitId(thread.getFile().getCommitId())
                    .setUser(User.newBuilder().setEmail(comment.getCreatedBy()).build())
                    .setBody(comment.getContent())
                    .setCreatedAt(Instant.ofEpochMilli(comment.getTimestamp()).toString())
                    .setPosition(getReviewCommentPosition(thread, githubPr))
                    .build())));
    return reviewComments;
  }

  private int getReviewCommentPosition(Thread thread, GithubPr githubPr) {
    LineNumberConverter.Side commentSide =
        thread.getFile().getCommitId().equals(thread.getCommitId())
            ? LineNumberConverter.Side.RIGHT
            : LineNumberConverter.Side.LEFT;
    String patch = "";
    try {
      patch =
          githubClient
              .getCommit(
                  CommitRequest.newBuilder()
                      .setOwner(githubPr.getRepo().getOwner())
                      .setRepo(githubPr.getRepo().getRepo())
                      .setSha(thread.getFile().getCommitId())
                      .build())
              .getCommit()
              .getFilesList()
              .stream()
              .filter(file -> thread.getFile().getFilename().equals(file.getFilename()))
              .collect(Collectors.toList())
              .get(0)
              .getPatch();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (patch.equals("")) {
      throw new RuntimeException(
          "Can not find the `patch` for file: " + thread.getFile().getFilename());
    }

    LineNumberConverter converter = new LineNumberConverter(patch);
    return converter.getPosition(thread.getLineNumber(), commentSide);
  }

  private List<IssueComment> getIssueComments(List<Thread> diffThreads) {
    List<IssueComment> result = new ArrayList<>();
    for (Protos.Thread thread : diffThreads) {
      thread
          .getCommentList()
          .forEach(
              comment ->
                  result.add(
                      IssueComment.newBuilder()
                          .setBody(comment.getContent())
                          .setCreatedAt(new Date(comment.getTimestamp()).toString())
                          .setUser(
                              GithubPullRequestProtos.User.newBuilder()
                                  .setEmail(comment.getCreatedBy())
                                  .build())
                          .build()));
    }
    return result;
  }
}

