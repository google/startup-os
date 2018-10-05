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
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHFile;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestFilesReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestsReq;
import com.google.startupos.tools.reviewer.localserver.service.Protos;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitHubWriter {
  private static final String DIFF_HUNK_PATTERN = "@@\\s[-+]\\d+[\\,]\\d+\\s[-+]\\d+[\\,]\\d+\\s@@";
  private final GitHubClient gitHubClient;

  GitHubWriter(GitHubClient gitHubClient) {
    this.gitHubClient = gitHubClient;
  }

  public void writeDiff(Diff diff, String repo) throws IOException {
    int diffNumber = getPRNumber(diff, repo);

    for (Thread diffThread : diff.getDiffThreadList()) {
      for (Protos.Comment comment : diffThread.getCommentList()) {
        createPullRequestComment(
            repo,
            diffNumber,
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + new Date(comment.getTimestamp())
                + "**\n"
                + comment.getContent());
      }
    }

    for (Thread codeThread : diff.getCodeThreadList()) {
      for (Protos.Comment comment : codeThread.getCommentList()) {
        String path = codeThread.getFile().getFilename();
        GHFile file = getFileByPath(repo, diffNumber, path);
        DiffHunk diffHunk = getDiffHunk(getDiffHunks(file.getPatch()), codeThread.getLineNumber());
        int position = getCommentPosition(diffHunk, codeThread.getLineNumber());
        createReviewComment(
            repo,
            diffNumber,
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + new Date(comment.getTimestamp())
                + "**\n"
                + comment.getContent(),
            codeThread.getCommitId(),
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
                    .setBody(body)
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

  private GHFile getFileByPath(String repo, int diffNumber, String path) throws IOException {
    List<GHFile> files =
        gitHubClient
            .getPullRequestFiles(
                GHPullRequestFilesReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getFilesList();
    for (GHFile file : files) {
      if (file.getFilename().equals(path)) {
        return file;
      }
    }
    throw new RuntimeException("File not found: " + path);
  }

  private List<DiffHunk> getDiffHunks(String patch) {
    List<DiffHunk> result = new ArrayList<>();
    List<String> bodies =
        Arrays.stream(patch.split(DIFF_HUNK_PATTERN))
            .filter(body -> !body.isEmpty())
            .collect(Collectors.toList());
    Pattern pattern = Pattern.compile(DIFF_HUNK_PATTERN);
    Matcher matcher = pattern.matcher(patch);
    int bodyIndex = 0;
    while (matcher.find()) {
      result.add(new DiffHunk(matcher.group() + bodies.get(bodyIndex++)));
    }
    return result;
  }

  private DiffHunk getDiffHunk(List<DiffHunk> diffHunks, int lineNumber) {
    if (diffHunks.size() == 1) {
      return diffHunks.get(0);
    }
    for (int i = 0; i < diffHunks.size(); i++) {
      DiffHunk current = diffHunks.get(i);
      boolean hasNext = i + 1 <= diffHunks.size() - 1;
      if (!hasNext) {
        return current;
      } else {
        DiffHunk next = diffHunks.get(i + 1);
        if ((current.getHeadStartLine() < lineNumber) && (next.getHeadStartLine() > lineNumber)) {
          return current;
        }
      }
    }
    throw new RuntimeException("The patch doesn't contain a diff hunk for the line: " + lineNumber);
  }

  // TODO: Check if it works correctly when the head does not contain the line where the comment was
  // left.
  private int getCommentPosition(DiffHunk diffHunk, int lineNumber) {
    int start = diffHunk.getHeadStartLine();
    if (diffHunk.isNewFileComment() || diffHunk.isCommentToTheFirstLine()) {
      return lineNumber;
    } else {
      lineNumber -= start;
      int result = start;
      List<String> newlineSymbols = new ArrayList<>();
      String patt = "\\n\\s|\\n\\+|\\n\\-";
      Pattern pattern = Pattern.compile(patt);
      Matcher matcher = pattern.matcher(diffHunk.getHunkBody());
      while (matcher.find()) {
        newlineSymbols.add(matcher.group());
      }
      for (String n : newlineSymbols) {
        if (n.equals("\n") || n.equals("\n+")) {
          lineNumber--;
        }
        if (lineNumber == 0) {
          return --result - start;
        }
        result++;
      }
      return result - start;
    }
  }
}

