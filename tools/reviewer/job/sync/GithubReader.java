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

import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.IssueCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.RepositoryCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.ReviewComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.IssueComment;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.CommitInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads GitHub Pull Request using GitHub API and creates
 * `tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest`
 */
public class GithubReader {
  private final GithubClient githubClient;

  public GithubReader(GithubClient githubClient) {
    this.githubClient = githubClient;
  }

  public PullRequest getPullRequest(String repoOwner, String repoName, long number)
      throws IOException {
    PullRequest.Builder pullRequest =
        githubClient
            .getPullRequest(
                PullRequestRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setNumber(number)
                    .build())
            .getPullRequest()
            .toBuilder();

    pullRequest
        .addAllReviewComment(getPullRequestReviewComments(repoOwner, repoName, number))
        .addAllIssueComment(getIssueComments(repoOwner, repoName, number))
        .addAllCommitsInfo(getCommits(repoOwner, repoName, number))
        .setRepo(repoName);
    return pullRequest.build();
  }

  private List<CommitInfo> getCommits(String repoOwner, String repoName, long number)
      throws IOException {
    List<CommitInfo> pullRequestCommits =
        githubClient
            .getCommits(
                CommitsRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setNumber(number)
                    .build())
            .getCommitsList();
    /* `https://developer.github.com/v3/pulls/#list-commits-on-a-pull-request` request doesn't provide
    files information. We send additional requests to get it. */
    List<CommitInfo> result = new ArrayList<>();
    for (CommitInfo commit : pullRequestCommits) {
      result.add(
          githubClient
              .getCommit(
                  CommitRequest.newBuilder()
                      .setOwner(repoOwner)
                      .setRepo(repoName)
                      .setSha(commit.getSha())
                      .build())
              .getCommit());
    }
    return result;
  }

  private List<IssueComment> getIssueComments(String repoOwner, String repoName, long number)
      throws IOException {
    return githubClient
        .getIssueComments(
            IssueCommentsRequest.newBuilder()
                .setOwner(repoOwner)
                .setRepo(repoName)
                .setNumber(number)
                .build())
        .getIssueCommentList();
  }

  private List<ReviewComment> getPullRequestReviewComments(
      String repoOwner, String repoName, long number) throws IOException {
    return githubClient
        .getRepositoryComments(
            RepositoryCommentsRequest.newBuilder().setOwner(repoOwner).setRepo(repoName).build())
        .getReviewCommentList()
        .stream()
        .filter(
            reviewComment ->
                Long.parseLong(
                        reviewComment
                            .getLinks()
                            .getPullRequest()
                            .getHref()
                            .substring(
                                reviewComment.getLinks().getPullRequest().getHref().lastIndexOf('/')
                                    + 1))
                    == number)
        .collect(Collectors.toList());
  }
}

