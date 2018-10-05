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
import com.google.startupos.common.repo.Protos;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Author;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHComment;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHPullRequestReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHUserReq;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GHCommentsReq;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitHubReader {
  private final GitHubClient gitHubClient;

  GitHubReader(GitHubClient gitHubClient) {
    this.gitHubClient = gitHubClient;
  }

  public Diff getDiff(String repo, int diffNumber) throws IOException {
    GHPullRequest pr =
        gitHubClient
            .getPullRequest(
                GHPullRequestReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getPullRequest();
    return Diff.newBuilder()
        .setId(Long.parseLong(pr.getTitle().replaceFirst("D", "")))
        .setAuthor(
            Author.newBuilder()
                .setEmail(
                    gitHubClient
                        .getUser(GHUserReq.newBuilder().setLogin(pr.getUser().getLogin()).build())
                        .getUser()
                        .getEmail())
                .build())
        .setDescription(pr.getBody())
        .setCreatedTimestamp(Instant.parse(pr.getCreatedAt()).toEpochMilli())
        .setModifiedTimestamp(Instant.parse(pr.getUpdatedAt()).toEpochMilli())
        .addAllCodeThread(
            getCodeThreads(
                pr,
                gitHubClient
                    .getReviewComments(
                        GHCommentsReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
                    .getCommentsList()))
        .addDiffThread(getDiffThread(repo, diffNumber))
        .build();
  }

  private Thread getDiffThread(String repo, int diffNumber) throws IOException {
    Thread.Builder diffThread = Thread.newBuilder();
    List<GHComment> diffComments =
        gitHubClient
            .getIssueComments(
                GHCommentsReq.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getCommentsList();

    diffComments.forEach(
        comment ->
            diffThread.addComment(
                Comment.newBuilder()
                    .setContent(comment.getBody())
                    .setTimestamp(Instant.parse(comment.getUpdatedAt()).getEpochSecond())
                    .setCreatedBy(
                        // TODO: Email can be null if the user hides email. Think over how to
                        // resolve this.
                        gitHubClient
                            .getUser(
                                GHUserReq.newBuilder()
                                    .setLogin(comment.getUser().getLogin())
                                    .build())
                            .getUser()
                            .getEmail())
                    .build()));
    return diffThread.build();
  }

  private ImmutableList<Thread> getCodeThreads(GHPullRequest pr, List<GHComment> comments) {
    List<Thread> result = new ArrayList<>();

    List<String> commentedFiles =
        comments.stream().map(GHComment::getPath).distinct().collect(Collectors.toList());

    for (String fileName : commentedFiles) {
      ImmutableList<GHComment> fileComments = getCodeCommentsByFilename(comments, fileName);
      List<Integer> lines =
          fileComments
              .stream()
              .filter(comment -> comment.getPath().equals(fileName))
              .map(
                  comment ->
                      getLineNumber(
                          new DiffHunk(comment.getDiffHunk()),
                          comment.getPosition(),
                          comment.getOriginalPosition()))
              .distinct()
              .collect(Collectors.toList());

      lines.forEach(line -> result.add(getCodeThreadByFilenameAndLine(fileComments, line, pr)));
    }
    return ImmutableList.copyOf(result);
  }

  private ImmutableList<GHComment> getCodeCommentsByFilename(
      List<GHComment> codeComments, String filename) {
    return ImmutableList.copyOf(
        codeComments
            .stream()
            .filter(comment -> comment.getPath().equals(filename))
            .collect(Collectors.toList()));
  }

  private Thread getCodeThreadByFilenameAndLine(
      ImmutableList<GHComment> fileComments, int line, GHPullRequest pr) {
    ImmutableList<GHComment> lineComments =
        ImmutableList.copyOf(
            fileComments
                .stream()
                .filter(
                    item ->
                        getLineNumber(
                                new DiffHunk(item.getDiffHunk()),
                                item.getPosition(),
                                item.getOriginalPosition())
                            == line)
                .collect(Collectors.toList()));

    Thread.Builder thread = Thread.newBuilder();
    lineComments.forEach(
        comment ->
            thread
                .setRepoId(pr.getTitle().replace("D", ""))
                .setCommitId(comment.getOriginalCommitId())
                .setFile(
                    Protos.File.newBuilder()
                        .setFilename(comment.getPath())
                        .setRepoId(pr.getTitle().replace("D", ""))
                        .setFilenameWithRepo(pr.getHead().getRepo().getFullName())
                        .setUser(
                            gitHubClient
                                .getUser(
                                    GHUserReq.newBuilder()
                                        .setLogin(pr.getUser().getLogin())
                                        .build())
                                .getUser()
                                .getEmail())
                        .build())
                .setLineNumber(
                    getLineNumber(
                        new DiffHunk(comment.getDiffHunk()),
                        comment.getPosition(),
                        comment.getOriginalPosition()))
                .addComment(
                    Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(Instant.parse(comment.getUpdatedAt()).getEpochSecond())
                        .setCreatedBy(
                            // TODO: Email can be null if the user hides email. Think over how to
                            // resolve this.
                            gitHubClient
                                .getUser(
                                    GHUserReq.newBuilder()
                                        .setLogin(comment.getUser().getLogin())
                                        .build())
                                .getUser()
                                .getEmail())
                        .build())
                .setType(Thread.Type.CODE));
    return thread.build();
  }

  private int getLineNumber(DiffHunk diffHunk, int position, int original_position) {
    // We are counting amount of `\n+` in diff_hunk.
    int commentPosition = diffHunk.getHunkBody().split("\\n\\+", -1).length - 1;

    if (diffHunk.isNewFileComment()) {
      return original_position;
    }
    if (position == 0) {
      return diffHunk.getHeadStartLine() + 3 + commentPosition;
    }
    if (diffHunk.isFileInHeadHasDifferentNumberOfLines()) {
      return diffHunk.getBaseStartLine() + 5 + commentPosition;
    }
    if (diffHunk.isCommentToTheFirstLine()) {
      return commentPosition;
    }
    return diffHunk.getHeadStartLine() + 2 + commentPosition;
  }
}

