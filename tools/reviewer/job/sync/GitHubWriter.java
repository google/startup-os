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

import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestCommentRequestData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestRequestData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateReviewCommentRequestData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.File;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestFilesRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestsRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class GitHubWriter {
  private final GitHubClient gitHubClient;

  GitHubWriter(GitHubClient gitHubClient) {
    this.gitHubClient = gitHubClient;
  }

  public void writeDiff(Diff diff, String repo) throws IOException {
    int diffNumber = getPRNumber(diff, repo);

    for (Thread diffThread : diff.getDiffThreadList()) {
      for (Comment comment : diffThread.getCommentList()) {
        // TODO: Think over what should be in the comment by Reviewer Bot
        createPullRequestComment(
            repo,
            diffNumber,
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + Instant.ofEpochSecond(comment.getTimestamp())
                + "**\n"
                + comment.getContent());
      }
    }

    List<File> pullRequestFiles =
        gitHubClient
            .getPullRequestFiles(
                PullRequestFilesRequest.newBuilder()
                    .setRepo(repo)
                    .setDiffNumber(diffNumber)
                    .build())
            .getFilesList();

    PullRequest pr =
        gitHubClient
            .getPullRequest(
                PullRequestRequest.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getPullRequest();

    for (Thread codeThread : diff.getCodeThreadList()) {
      for (Comment comment : codeThread.getCommentList()) {
        String path = codeThread.getFile().getFilename();
        String diffPatchStr = getPatchStrByFilename(pullRequestFiles, path);
        LineNumberConverter.Side side =
            getCommentSide(codeThread.getFile().getCommitId(), codeThread.getCommitId());
        int position = getCommentPosition(diffPatchStr, codeThread.getLineNumber(), side);
        // TODO: Think over what should be in the comment by Reviewer Bot
        createReviewComment(
            repo,
            diffNumber,
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + Instant.ofEpochSecond(comment.getTimestamp()).toString()
                + "**\n"
                + comment.getContent(),
            pr.getHead().getSha(),
            path,
            position);
      }
    }
  }

  private int getPRNumber(Diff diff, String repo) {
    PullRequestsRequest request = PullRequestsRequest.newBuilder().setRepo(repo).build();
    try {
      List<PullRequest> pullRequests = gitHubClient.getPullRequests(request).getPullRequestsList();
      for (PullRequest pr : pullRequests) {
        if (pr.getTitle().equals("D" + diff.getId())) {
          return pr.getNumber();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Creating new PR if it isn't exist
    return createPullRequest(
        repo, "D" + diff.getId(), "D" + diff.getId(), "master", diff.getDescription());
  }

  private int createPullRequest(String repo, String title, String head, String base, String body) {
    CreatePullRequestRequest request =
        CreatePullRequestRequest.newBuilder()
            .setRepo(repo)
            .setRequestData(
                CreatePullRequestRequestData.newBuilder()
                    .setTitle(title)
                    .setHead(head)
                    .setBase(base)
                    // TODO: `body`(PullRequest description) can't be empty. Think over the default
                    // value
                    .setBody(body.isEmpty() ? "Created by Reviewer Bot" : body)
                    .build())
            .build();
    CreatePullRequestResponse response = gitHubClient.createPullRequest(request);
    return response.getPullRequest().getNumber();
  }

  private void createPullRequestComment(String repo, int diffNumber, String body) {
    CreatePullRequestCommentRequest request =
        CreatePullRequestCommentRequest.newBuilder()
            .setRepo(repo)
            .setDiffNumber(diffNumber)
            .setRequestData(CreatePullRequestCommentRequestData.newBuilder().setBody(body).build())
            .build();
    gitHubClient.createPullRequestComment(request);
  }

  private void createReviewComment(
      String repo, int diffNumber, String body, String commitId, String path, int position) {
    CreateReviewCommentRequest request =
        CreateReviewCommentRequest.newBuilder()
            .setRepo(repo)
            .setDiffNumber(diffNumber)
            .setRequestData(
                CreateReviewCommentRequestData.newBuilder()
                    .setBody(body)
                    .setCommitId(commitId)
                    .setPath(path)
                    .setPosition(position)
                    .build())
            .build();
    gitHubClient.createReviewComment(request);
  }

  private int getCommentPosition(String patchStr, int lineNumber, LineNumberConverter.Side side) {
    return new LineNumberConverter(patchStr).getPosition(lineNumber, side);
  }

  private LineNumberConverter.Side getCommentSide(
      String repoBaseCommitId, String codeThreadCommitId) {
    return repoBaseCommitId.equals(codeThreadCommitId)
        ? LineNumberConverter.Side.RIGHT
        : LineNumberConverter.Side.LEFT;
  }

  private String getPatchStrByFilename(List<File> pullRequestFiles, String filename) {
    for (File file : pullRequestFiles) {
      if (file.getFilename().equals(filename)) {
        return file.getPatch();
      }
    }
    throw new RuntimeException("`patch` not found for the file: " + filename);
  }
}

