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

import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestCommentRequestData;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequestData;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentRequestData;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.File;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestFilesRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestsRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/** Writes `tools.reviewer.localserver.service.Protos.Diff` to GitHub using GitHub API */
public class GithubWriter {
  private final String reviewerDiffLink;
  private final GithubClient githubClient;

  GithubWriter(GithubClient githubClient, String reviewerDiffLink) {
    this.githubClient = githubClient;
    this.reviewerDiffLink = reviewerDiffLink;
  }

  public void writeDiff(Diff diff, String repoOwner, String repoName) throws IOException {
    int diffNumber = getGithubPullRequestNumber(diff, repoOwner, repoName);

    for (Thread diffThread : diff.getDiffThreadList()) {
      for (Comment comment : diffThread.getCommentList()) {
        // TODO: Think over what should be in the comment by Reviewer Bot
        createPullRequestComment(
            repoOwner,
            repoName,
            diffNumber,
            "Created by: "
                + comment.getCreatedBy()
                + "\nTime: "
                + Instant.ofEpochSecond(comment.getTimestamp())
                + "\n"
                + comment.getContent());
      }
    }

    List<File> pullRequestFiles =
        githubClient
            .getPullRequestFiles(
                PullRequestFilesRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setDiffNumber(diffNumber)
                    .build())
            .getFilesList();

    PullRequest pr =
        githubClient
            .getPullRequest(
                PullRequestRequest.newBuilder()
                    .setOwner(repoOwner)
                    .setRepo(repoName)
                    .setDiffNumber(diffNumber)
                    .build())
            .getPullRequest();

    for (Thread codeThread : diff.getCodeThreadList()) {
      String reviewerLink =
          reviewerDiffLink + diffNumber + "/" + codeThread.getFile().getFilenameWithRepo();
      for (Comment comment : codeThread.getCommentList()) {
        String path = codeThread.getFile().getFilename();
        String diffPatchStr = getPatchStrByFilename(pullRequestFiles, path);
        LineNumberConverter.Side side =
            getCommentSide(codeThread.getFile().getCommitId(), codeThread.getCommitId());
        int position = getCommentPosition(diffPatchStr, codeThread.getLineNumber(), side);
        // TODO: Think over what should be in the comment by Reviewer Bot
        createReviewComment(
            repoOwner,
            repoName,
            diffNumber,
            "Created by: "
                + comment.getCreatedBy()
                + "\nTime: "
                + Instant.ofEpochSecond(comment.getTimestamp()).toString()
                + "\nBody: "
                + comment.getContent()
                + "\nSee in Reviewer: "
                + reviewerLink,
            pr.getHead().getSha(),
            path,
            position);
      }
    }
  }

  private int getGithubPullRequestNumber(Diff diff, String repoOwner, String repoName) {
    PullRequestsRequest request =
        PullRequestsRequest.newBuilder().setOwner(repoOwner).setRepo(repoName).build();
    try {
      List<PullRequest> pullRequests = githubClient.getPullRequests(request).getPullRequestsList();
      for (PullRequest pr : pullRequests) {
        if (pr.getTitle().equals("D" + diff.getId())) {
          return pr.getNumber();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Create new Pull Request if it doesn't exist
    return createPullRequest(
        repoOwner,
        repoName,
        "D" + diff.getId(),
        "D" + diff.getId(),
        "master",
        diff.getDescription());
  }

  private int createPullRequest(
      String repoOwner, String repoName, String title, String head, String base, String body) {
    CreatePullRequestRequest request =
        CreatePullRequestRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
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
    CreatePullRequestResponse response = githubClient.createPullRequest(request);
    return response.getPullRequest().getNumber();
  }

  private void createPullRequestComment(
      String repoOwner, String repoName, int diffNumber, String body) {
    CreatePullRequestCommentRequest request =
        CreatePullRequestCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setDiffNumber(diffNumber)
            .setRequestData(CreatePullRequestCommentRequestData.newBuilder().setBody(body).build())
            .build();
    githubClient.createPullRequestComment(request);
  }

  private void createReviewComment(
      String repoOwner,
      String repoName,
      int diffNumber,
      String body,
      String commitId,
      String path,
      int position) {
    CreateReviewCommentRequest request =
        CreateReviewCommentRequest.newBuilder()
            .setOwner(repoOwner)
            .setRepo(repoName)
            .setDiffNumber(diffNumber)
            .setRequestData(
                CreateReviewCommentRequestData.newBuilder()
                    .setBody(body)
                    .setCommitId(commitId)
                    .setPath(path)
                    .setPosition(position)
                    .build())
            .build();
    githubClient.createReviewComment(request);
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
    throw new RuntimeException("`Patch` not found for the file: " + filename);
  }
}

