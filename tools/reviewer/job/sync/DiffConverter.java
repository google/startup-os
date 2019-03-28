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
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.User;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Comment;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Diff;
import com.google.startupos.tools.reviewer.local_server.service.Protos.GithubPr;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Thread;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// Converts a Diff proto to a list of GithubPullRequestProtos.PullRequest protos
public class DiffConverter {

  private ReviewerClient reviewerClient;

  public DiffConverter(ReviewerClient reviewerClient) {
    this.reviewerClient = reviewerClient;
  }

  public ImmutableList<PullRequest> toPullRequests(
      Diff diff, ImmutableMap<String, GitRepo> repoNameToGitRepos) {
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
                  repoNameToGitRepos,
                  diff.getModifiedTimestamp(),
                  diff.getId()))
          .addAllIssueComment(getIssueComments(diff.getDiffThreadList(), githubPr.getRepo()))
          .setOwner(githubPr.getOwner())
          .setAssociatedReviewerDiff(diff.getId());
      pullRequests.add(pullRequest.build());
    }
    return pullRequests.build();
  }

  private ImmutableList<ReviewComment> getReviewCommentsByRepoName(
      List<Thread> codeThreads,
      String repoName,
      ImmutableMap<String, GitRepo> repoNameToGitRepos,
      long modifiedTimestamp,
      long diffId) {
    ImmutableList.Builder<ReviewComment> result = ImmutableList.builder();
    ImmutableList<Thread> codeThreadsByRepo =
        ImmutableList.copyOf(
            codeThreads.stream()
                .filter(thread -> thread.getRepoId().equals(repoName))
                .collect(Collectors.toList()));

    for (Thread thread : codeThreadsByRepo) {
      for (Comment comment : thread.getCommentList()) {
        GitRepo gitRepo = getGitRepo(thread.getFile().getRepoId(), repoNameToGitRepos);

        int githubCommentPosition;
        String commitId;
        boolean isOutsideDiffComment = false;
        // `0` value means the comment isn't synced with GitHub
        if (thread.getGithubCommentPosition() == 0
            && thread.getClosestGithubCommentPosition() == 0) {
          String filename = thread.getFile().getFilename();
          String baseBranchCommitId = gitRepo.getMostRecentCommitOfBranch("master");

          String patch;
          if (baseBranchCommitId.equals(thread.getFile().getCommitId())) {
            // for the left file
            commitId = gitRepo.getMostRecentCommitOfFile(filename);
            patch = gitRepo.getPatch(baseBranchCommitId, commitId, filename);
          } else {
            // for the right file
            commitId = thread.getFile().getCommitId();
            patch = gitRepo.getPatch(baseBranchCommitId, commitId, filename);
          }
          LineNumberConverter.LineNumberToGithubPositionCorrelation correlation =
              getGithubReviewCommentPosition(baseBranchCommitId, thread, patch);
          if (correlation.getExactGithubPosition() != 0) {
            githubCommentPosition = correlation.getExactGithubPosition();
          } else {
            githubCommentPosition = correlation.getClosestGithubPosition();
            isOutsideDiffComment = true;
          }
          reviewerClient.addGithubReviewCommentPosition(
              diffId, thread.getId(), githubCommentPosition, comment.getId());
        } else {
          if (thread.getGithubCommentPosition() != 0) {
            githubCommentPosition = thread.getGithubCommentPosition();
          } else {
            githubCommentPosition = thread.getClosestGithubCommentPosition();
            isOutsideDiffComment = true;
          }
          commitId = thread.getFile().getCommitId();
        }

        ReviewComment.Builder githubComment = ReviewComment.newBuilder();
        githubComment
            .setPath(thread.getFile().getFilename())
            .setId(comment.getGithubCommentId())
            .setPosition(githubCommentPosition)
            .setCommitId(commitId)
            .setUser(User.newBuilder().setEmail(comment.getCreatedBy()).build())
            .setBody(comment.getContent())
            .setCreatedAt(String.valueOf(Instant.ofEpochMilli(comment.getTimestamp())))
            .setUpdatedAt(String.valueOf(Instant.ofEpochMilli(modifiedTimestamp)))
            .setReviewerThreadId(thread.getId())
            .setReviewerCommentId(comment.getId())
            .setIsOutsideDiffComment(isOutsideDiffComment)
            .setReviewerLineNumber(thread.getLineNumber())
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

  private LineNumberConverter.LineNumberToGithubPositionCorrelation getGithubReviewCommentPosition(
      String baseBranchCommitId, Thread thread, String patch) {
    LineNumberConverter.Side commentSide =
        baseBranchCommitId.equals(thread.getCommitId())
            ? LineNumberConverter.Side.LEFT
            : LineNumberConverter.Side.RIGHT;

    if (patch.equals("")) {
      throw new RuntimeException("Patch is empty for file: " + thread.getFile().getFilename());
    }
    return LineNumberConverter.getPosition(patch, thread.getLineNumber(), commentSide);
  }

  private ImmutableList<IssueComment> getIssueComments(List<Thread> diffThreads, String repoName) {
    ImmutableList.Builder<IssueComment> result = ImmutableList.builder();
    ImmutableList<Thread> diffThreadsByRepo =
        ImmutableList.copyOf(
            diffThreads.stream()
                .filter(thread -> thread.getRepoId().equals(repoName))
                .collect(Collectors.toList()));
    for (Thread thread : diffThreadsByRepo) {
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
                          .setReviewerThreadId(thread.getId())
                          .setReviewerCommentId(comment.getId())
                          .build()));
    }
    return result.build();
  }

  private GitRepo getGitRepo(String repoName, ImmutableMap<String, GitRepo> repoNameToGitRepos) {
    if (!repoNameToGitRepos.containsKey(repoName)) {
      throw new IllegalArgumentException("Cant find the git repo: " + repoName);
    }
    return repoNameToGitRepos.get(repoName);
  }
}

