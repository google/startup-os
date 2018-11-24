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

/* Converts `tools.reviewer.localserver.service.Protos.Diff`
to List<tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest> */
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

      if (githubPr.getNumber() != 0) {
        pullRequest.setNumber(githubPr.getNumber());
      }
      pullRequest
          .setState(getPullRequestState(diff.getStatus()))
          .setTitle("D" + diff.getId())
          .setUser(User.newBuilder().setEmail(diff.getAuthor().getEmail()).build())
          // TODO: Set more informative message for default description
          .setBody(diff.getDescription().isEmpty() ? "Default description" : diff.getDescription())
          .setCreatedAt(Instant.ofEpochMilli(diff.getCreatedTimestamp()).toString())
          .setUpdatedAt(Instant.ofEpochMilli(diff.getModifiedTimestamp()).toString())
          .setBase(getCommitPointer(githubPr, CommitPointerType.BASE, "D" + diff.getId()))
          .setHead(getCommitPointer(githubPr, CommitPointerType.HEAD, "D" + diff.getId()))
          .setRepo(githubPr.getRepo().getRepo())
          .addAllReviewComment(getReviewCommentsByRepoName(diff, githubPr))
          .addAllIssueComment(getIssueComments(diff, githubPr))
          .setOwner(githubPr.getRepo().getOwner())
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
            .filter(thread -> thread.getRepoId().equals(githubPr.getRepo().getRepo()))
            .collect(Collectors.toList());

    for (Thread thread : codeThreads) {
      for (Comment comment : thread.getCommentList()) {
        ReviewComment.Builder githubComment = ReviewComment.newBuilder();
        githubComment
            .setPath(thread.getFile().getFilename())
            .setId(comment.getGithubCommentId())
            .setPosition(getReviewCommentPosition(thread, githubPr))
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

  private CommitPointer getCommitPointer(
      GithubPr githubPr, CommitPointerType commitPointerType, String headBranch)
      throws IOException {

    if (githubPr.getNumber() == 0) {
      CommitPointer.Builder commitPointer = CommitPointer.newBuilder();
      commitPointer.setRef(
          commitPointerType.equals(CommitPointerType.BASE) ? "master" : headBranch);
      return commitPointer.build();
    } else {
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

  private List<IssueComment> getIssueComments(Diff diff, GithubPr githubPr) {
    List<IssueComment> result = new ArrayList<>();
    List<Thread> diffThreads =
        diff.getDiffThreadList()
            .stream()
            .filter(thread -> thread.getRepoId().equals(githubPr.getRepo().getRepo()))
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

