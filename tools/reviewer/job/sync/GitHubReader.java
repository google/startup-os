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
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Author;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitHubReader {

  public Diff getDiff(String repoName, int diffNumber, String login, String password)
      throws IOException {
    final GitHub gitHub = GitHub.connectUsingPassword(login, password);
    final GHRepository repository = gitHub.getRepository(repoName);
    final GHPullRequest pullRequest = repository.getPullRequest(diffNumber);
    ImmutableList<GHPullRequestReviewComment> codeComments =
        ImmutableList.copyOf(pullRequest.listReviewComments().asList());

    // We aren't syncing reviewers, bug, status, and workspace initially.
    // TODO: Set id for Diff
    return Diff.newBuilder()
        .setAuthor(Author.newBuilder().setEmail(pullRequest.getUser().getEmail()).build())
        .setDescription(pullRequest.getBody())
        .setCreatedTimestamp(pullRequest.getCreatedAt().getTime())
        .setModifiedTimestamp(pullRequest.getUpdatedAt().getTime())
        .addAllCodeThread(getCodeThreads(codeComments, pullRequest))
        .addDiffThread(getDiffThread(pullRequest))
        .build();
  }

  private Thread getDiffThread(GHPullRequest pullRequest) throws IOException {
    ImmutableList<GHIssueComment> diffComments = ImmutableList.copyOf(pullRequest.getComments());
    Thread.Builder thread = Thread.newBuilder();
    diffComments.forEach(
        comment -> {
          try {
            thread
                .addComment(
                    Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(comment.getUpdatedAt().getTime())
                        .setCreatedBy(comment.getUser().getName())
                        .build())
                .setType(Thread.Type.DIFF);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    return thread.build();
  }

  private ImmutableList<Thread> getCodeThreads(
      ImmutableList<GHPullRequestReviewComment> codeComments, GHPullRequest pullRequest) {

    List<Thread> result = new ArrayList<>();
    ImmutableList<String> commentedFiles =
        ImmutableList.copyOf(
            codeComments
                .stream()
                .map(GHPullRequestReviewComment::getPath)
                .distinct()
                .collect(Collectors.toList()));

    for (String fileName : commentedFiles) {
      ImmutableList<GHPullRequestReviewComment> fileComments =
          getCodeCommentsByFilename(codeComments, fileName);
      ImmutableList<Integer> lines =
          ImmutableList.copyOf(
              fileComments
                  .stream()
                  .filter(comment -> comment.getPath().equals(fileName))
                  // TODO: Think over how to get value the same as the line number in the file.
                  // Currently, the value is position within the diff
                  .map(GHPullRequestReviewComment::getOriginalPosition)
                  .distinct()
                  .collect(Collectors.toList()));
      lines.forEach(
          line -> result.add(getCodeThreadByFilenameAndLine(fileComments, line, pullRequest)));
    }
    return ImmutableList.copyOf(result);
  }

  private ImmutableList<GHPullRequestReviewComment> getCodeCommentsByFilename(
      ImmutableList<GHPullRequestReviewComment> codeComments, String filename) {
    return ImmutableList.copyOf(
        codeComments
            .stream()
            .filter(comment -> comment.getPath().equals(filename))
            .collect(Collectors.toList()));
  }

  private Thread getCodeThreadByFilenameAndLine(
      ImmutableList<GHPullRequestReviewComment> fileComments, int line, GHPullRequest pullRequest) {

    ImmutableList<GHPullRequestReviewComment> lineComments =
        ImmutableList.copyOf(
            fileComments
                .stream()
                .filter(comment -> comment.getOriginalPosition() == line)
                .collect(Collectors.toList()));

    Thread.Builder thread = Thread.newBuilder();
    lineComments.forEach(
        comment -> {
          try {
            thread
                // TODO: Set repoId, commitId, isDone for Thread
                .setFile(
                    // TODO: Set workspace, repoId, commitId, action for File in Thread
                    com.google.startupos.common.repo.Protos.File.newBuilder()
                        .setFilename(comment.getPath())
                        .setFilenameWithRepo(
                            pullRequest.getRepository().getFullName() + "/" + comment.getPath())
                        .setUser(pullRequest.getUser().getName())
                        .build())
                .setLineNumber(line)
                .addComment(
                    Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(comment.getUpdatedAt().getTime())
                        .setCreatedBy(comment.getUser().getName())
                        .build())
                .setType(Thread.Type.CODE);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    return thread.build();
  }
}

