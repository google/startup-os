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
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.GitHubComment;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.UserRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestFilesRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.File;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitHubReader {
  // The regex pattern for newline symbols search:
  // `\\n\\s` - newline with space,
  // `\\n\\+` - newline with `+` character,
  // `\\n\\-` - newline with `-` character
  private static final String NEW_LINES_PATTERN = "\\n\\s|\\n\\+|\\n\\-";
  private final GitHubClient gitHubClient;

  GitHubReader(GitHubClient gitHubClient) {
    this.gitHubClient = gitHubClient;
  }

  public Diff getDiff(String repo, int diffNumber) throws IOException {
    PullRequest pr =
        gitHubClient
            .getPullRequest(
                PullRequestRequest.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
            .getPullRequest();
    return Diff.newBuilder()
        .setId(Long.parseLong(pr.getTitle().replaceFirst("D", "")))
        .setAuthor(
            Author.newBuilder()
                .setEmail(
                    gitHubClient
                        .getUser(UserRequest.newBuilder().setLogin(pr.getUser().getLogin()).build())
                        .getUser()
                        .getEmail())
                .build())
        .setDescription(pr.getBody())
        .setCreatedTimestamp(Instant.parse(pr.getCreatedAt()).getEpochSecond())
        .setModifiedTimestamp(Instant.parse(pr.getUpdatedAt()).getEpochSecond())
        .addAllCodeThread(
            getCodeThreads(
                pr,
                gitHubClient
                    .getReviewComments(
                        CommentsRequest.newBuilder()
                            .setRepo(repo)
                            .setDiffNumber(diffNumber)
                            .build())
                    .getCommentsList(),
                repo,
                diffNumber))
        .addDiffThread(getDiffThread(repo, diffNumber))
        .build();
  }

  private Thread getDiffThread(String repo, int diffNumber) throws IOException {
    Thread.Builder diffThread = Thread.newBuilder();
    List<GitHubComment> diffComments =
        gitHubClient
            .getIssueComments(
                CommentsRequest.newBuilder().setRepo(repo).setDiffNumber(diffNumber).build())
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
                                UserRequest.newBuilder()
                                    .setLogin(comment.getUser().getLogin())
                                    .build())
                            .getUser()
                            .getEmail())
                    .build()));
    return diffThread.build();
  }

  private ImmutableList<Thread> getCodeThreads(
      PullRequest pr, List<GitHubComment> comments, String repo, int diffNumber)
      throws IOException {
    List<Thread> result = new ArrayList<>();

    List<String> commentedFiles =
        comments.stream().map(GitHubComment::getPath).distinct().collect(Collectors.toList());

    List<File> pullRequestFiles =
        gitHubClient
            .getPullRequestFiles(
                PullRequestFilesRequest.newBuilder()
                    .setRepo(repo)
                    .setDiffNumber(diffNumber)
                    .build())
            .getFilesList();

    for (String fileName : commentedFiles) {
      ImmutableList<GitHubComment> fileComments = getCodeCommentsByFilename(comments, fileName);
      List<Integer> positions =
          fileComments
              .stream()
              .filter(comment -> comment.getPath().equals(fileName))
              .map(GitHubComment::getPosition)
              .distinct()
              .collect(Collectors.toList());

      positions.forEach(
          position ->
              result.add(
                  getCodeThreadByFilenameAndPosition(
                      fileComments, position, pr, pullRequestFiles)));
    }
    return ImmutableList.copyOf(result);
  }

  private ImmutableList<GitHubComment> getCodeCommentsByFilename(
      List<GitHubComment> codeComments, String filename) {
    return ImmutableList.copyOf(
        codeComments
            .stream()
            .filter(comment -> comment.getPath().equals(filename))
            .collect(Collectors.toList()));
  }

  private Thread getCodeThreadByFilenameAndPosition(
      ImmutableList<GitHubComment> fileComments,
      int position,
      PullRequest pr,
      List<File> pullRequestFiles) {
    ImmutableList<GitHubComment> positionComments =
        ImmutableList.copyOf(
            fileComments
                .stream()
                .filter(comment -> comment.getPosition() == position)
                .collect(Collectors.toList()));

    Thread.Builder thread = Thread.newBuilder();
    positionComments.forEach(
        comment ->
            thread
                .setRepoId(pr.getTitle().replace("D", ""))
                .setCommitId(setThreadCommitId(pr, comment))
                .setFile(
                    Protos.File.newBuilder()
                        .setFilename(comment.getPath())
                        .setRepoId(pr.getTitle().replace("D", ""))
                        .setFilenameWithRepo(pr.getHead().getRepo().getFullName())
                        .setCommitId(comment.getCommitId())
                        .setUser(
                            gitHubClient
                                .getUser(
                                    UserRequest.newBuilder()
                                        .setLogin(pr.getUser().getLogin())
                                        .build())
                                .getUser()
                                .getEmail())
                        .build())
                .setLineNumber(
                    getLineNumber(
                        getDiffPatchStrByFilename(pullRequestFiles, comment.getPath()),
                        comment.getPosition(),
                        getCommentSide(comment.getDiffHunk())))
                .addComment(
                    Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(Instant.parse(comment.getUpdatedAt()).getEpochSecond())
                        .setCreatedBy(
                            // TODO: Email can be null if the user hides email. Think over how to
                            // resolve this.
                            gitHubClient
                                .getUser(
                                    UserRequest.newBuilder()
                                        .setLogin(comment.getUser().getLogin())
                                        .build())
                                .getUser()
                                .getEmail())
                        .build())
                .setType(Thread.Type.CODE));
    return thread.build();
  }

  private LineNumberConverter.Side getCommentSide(String commentDiffHunk) {
    List<String> newLinesSymbols = new ArrayList<>();
    Pattern pattern = Pattern.compile(NEW_LINES_PATTERN);
    Matcher matcher = pattern.matcher(commentDiffHunk);
    while (matcher.find()) {
      newLinesSymbols.add(matcher.group());
    }
    String lastNewlineSymbol = newLinesSymbols.get(newLinesSymbols.size() - 1);
    // If the last newline symbol in comment diff hunk is `\n+`, it means the comment was left to
    // the right side, otherwise - to the left side.
    return lastNewlineSymbol.equals("\n+")
        ? LineNumberConverter.Side.RIGHT
        : LineNumberConverter.Side.LEFT;
  }

  private String getDiffPatchStrByFilename(List<File> pullRequestFiles, String filename) {
    System.out.println(filename);
    for (File file : pullRequestFiles) {
      System.out.println(file.getStatus() + " : " + file.getFilename());
      if (file.getFilename().equals(filename)) {
        return file.getPatch();
      }
    }
    throw new RuntimeException("`diff_patch` not found for the file: " + filename);
  }

  private String setThreadCommitId(PullRequest pr, GitHubComment comment) {
    LineNumberConverter.Side side = getCommentSide(comment.getDiffHunk());
    return side.equals(LineNumberConverter.Side.LEFT)
        ? pr.getBase().getSha()
        : comment.getCommitId();
  }

  private int getLineNumber(String diffPatchStr, int position, LineNumberConverter.Side side) {
    return new LineNumberConverter(diffPatchStr).getLineNumb(position, side);
  }
}

