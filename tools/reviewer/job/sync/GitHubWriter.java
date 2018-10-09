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

import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHPullRequestCommentReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHPullRequestCommentReqData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHPullRequestReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHPullRequestReqData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHPullRequestResp;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHReviewCommentReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateGHReviewCommentReqData;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHFile;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestFilesReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestsReq;
import com.google.startupos.tools.reviewer.localserver.service.Protos;
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
      for (Protos.Comment comment : diffThread.getCommentList()) {
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

    List<GHFile> pullRequestFiles =
        gitHubClient
            .getPullRequestFiles(
                GHPullRequestFilesReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getFilesList();

    GHPullRequest pr =
        gitHubClient
            .getPullRequest(
                GHPullRequestReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getPullRequest();

    for (Thread codeThread : diff.getCodeThreadList()) {
      for (Protos.Comment comment : codeThread.getCommentList()) {
        String path = codeThread.getFile().getFilename();
        String diffPatchStr = getDiffPatchStrByFilename(pullRequestFiles, path);
        LineNumberConverter.Side side =
            getSide(codeThread.getFile().getCommitId(), codeThread.getCommitId());
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
    GHPullRequestsReq request = GHPullRequestsReq.newBuilder().setRepo(repo).build();
    try {
      List<GHPullRequest> pullRequests =
          gitHubClient.getPullRequests(request).getPullRequestsList();
      for (GHPullRequest pr : pullRequests) {
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
    CreateGHPullRequestReq request =
        CreateGHPullRequestReq.newBuilder()
            .setRepo(repo)
            .setRequestData(
                CreateGHPullRequestReqData.newBuilder()
                    .setTitle(title)
                    .setHead(head)
                    .setBase(base)
                    // TODO: `body`(PullRequest description) can't be empty. Think over the default
                    // value
                    .setBody(body.isEmpty() ? "Created by Reviewer Bot" : body)
                    .build())
            .build();
    CreateGHPullRequestResp response = gitHubClient.createPullRequest(request);
    return response.getPullRequest().getNumber();
  }

  private void createPullRequestComment(String repo, int diffNumber, String body) {
    CreateGHPullRequestCommentReq request =
        CreateGHPullRequestCommentReq.newBuilder()
            .setRepo(repo)
            .setDiffNumber(diffNumber)
            .setRequestData(CreateGHPullRequestCommentReqData.newBuilder().setBody(body).build())
            .build();
    gitHubClient.createPullRequestComment(request);
  }

  private void createReviewComment(
      String repo, int diffNumber, String body, String commitId, String path, int position) {
    CreateGHReviewCommentReq request =
        CreateGHReviewCommentReq.newBuilder()
            .setRepo(repo)
            .setDiffNumber(diffNumber)
            .setRequestData(
                CreateGHReviewCommentReqData.newBuilder()
                    .setBody(body)
                    .setCommitId(commitId)
                    .setPath(path)
                    .setPosition(position)
                    .build())
            .build();
    gitHubClient.createReviewComment(request);
  }

  private int getCommentPosition(
      String diffPatchStr, int lineNumber, LineNumberConverter.Side side) {
    return new LineNumberConverter(diffPatchStr).getPosition(lineNumber, side);
  }

  private LineNumberConverter.Side getSide(String repoBaseCommitId, String codeThreadCommitId) {
    return repoBaseCommitId.equals(codeThreadCommitId)
        ? LineNumberConverter.Side.RIGHT
        : LineNumberConverter.Side.LEFT;
  }

  private String getDiffPatchStrByFilename(List<GHFile> pullRequestFiles, String filename) {
    for (GHFile file : pullRequestFiles) {
      if (file.getFilename().equals(filename)) {
        return file.getPatch();
      }
    }
    throw new RuntimeException("`diff_patch` not found for the file: " + filename);
  }
}

