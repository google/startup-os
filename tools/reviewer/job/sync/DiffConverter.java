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
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
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
import java.util.List;
import java.util.stream.Collectors;

// Converts a Diff proto to a list of GithubPullRequestProtos.PullRequest protos
public class DiffConverter {

  private GithubClient githubClient;

  public DiffConverter(GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  public List<PullRequest> toPullRequests(Diff diff) {
    List<PullRequest> pullRequests = new ArrayList<>();

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
          .addAllReviewComment(getReviewCommentsByRepoName(diff, githubPr))
          .addAllIssueComment(getIssueComments(diff, githubPr))
          .setOwner(githubPr.getOwner())
          .setAssociatedReviewerDiff(diff.getId());
      pullRequests.add(pullRequest.build());
    }
    return pullRequests;
  }

  private List<ReviewComment> getReviewCommentsByRepoName(Diff diff, GithubPr githubPr) {
    List<ReviewComment> result = new ArrayList<>();
    List<Thread> codeThreads =
        diff.getCodeThreadList()
            .stream()
            .filter(thread -> thread.getRepoId().equals(githubPr.getRepo()))
            .collect(Collectors.toList());

    for (Thread thread : codeThreads) {
      for (Comment comment : thread.getCommentList()) {
        ReviewComment.Builder githubComment = ReviewComment.newBuilder();
        githubComment
            .setPath(thread.getFile().getFilename())
            .setId(comment.getGithubCommentId())
            .setPosition(
                thread.getGithubCommentPosition() == 0
                    ? getReviewCommentPosition(thread, githubPr)
                    : thread.getGithubCommentPosition())
            .setCommitId(thread.getFile().getCommitId())
            .setUser(User.newBuilder().setEmail(comment.getCreatedBy()).build())
            .setBody(comment.getContent())
            .setCreatedAt(String.valueOf(Instant.ofEpochMilli(comment.getTimestamp())))
            .setReviewerThreadId(thread.getId())
            .build();
        result.add(githubComment.build());
      }
    }
    return result;
  }

  // TODO: Check if the state is correct
  private String getPullRequestState(Diff.Status status) {
    if (status.equals(Diff.Status.ACCEPTED)) {
      return "close";
    } else {
      return "open";
    }
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
                      .setOwner(githubPr.getOwner())
                      .setRepo(githubPr.getRepo())
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

  private List<IssueComment> getIssueComments(Diff diff, GithubPr githubPr) {
    List<IssueComment> result = new ArrayList<>();
    List<Thread> diffThreads =
        diff.getDiffThreadList()
            .stream()
            .filter(thread -> thread.getRepoId().equals(githubPr.getRepo()))
            .collect(Collectors.toList());
    for (Protos.Thread thread : diffThreads) {
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
    return result;
  }
}

