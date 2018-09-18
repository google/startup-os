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

import com.google.startupos.tools.reviewer.localserver.service.Protos;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipientGitHubDiff {
  private GHPullRequest pullRequest;
  private List<GHPullRequestReviewComment> commentList;
  private String repoName;

  public RecipientGitHubDiff(String repoName, int diffNumber, String login, String password)
      throws IOException {
    this.repoName = repoName;
    GitHub gitHub = GitHub.connectUsingPassword(login, password);
    GHRepository repository = gitHub.getRepository(repoName);
    pullRequest = repository.getPullRequest(diffNumber);
    commentList = pullRequest.listReviewComments().asList();
  }

  public Protos.Diff getDiff() throws IOException {

    Protos.Diff.Builder diff = Protos.Diff.newBuilder();

    // diff.setId();
    diff.setAuthor(Protos.Author.newBuilder().setEmail(pullRequest.getUser().getEmail()).build());

    //    diff.addReviewer(Protos.Reviewer.newBuilder()
    //        .setEmail(pullRequest.listReviews().asList().get(1).)
    //        .setNeedsAttention()
    //        .setApproved()
    //        .build());
    diff.setDescription(pullRequest.getBody());
    //    diff.setBug();
    //    diff.setStatus();
    //    diff.setWorkspace();
    diff.setCreatedTimestamp(pullRequest.getCreatedAt().getTime());
    diff.setModifiedTimestamp(pullRequest.getUpdatedAt().getTime());
    diff.addAllCodeThread(getCodeThreads());
    diff.addDiffThread(getDiffThread());

    return diff.build();
  }

  private Protos.Thread getDiffThread() throws IOException {
    List<GHIssueComment> issueComments = pullRequest.getComments();
    Protos.Thread.Builder thread = Protos.Thread.newBuilder();
    issueComments.forEach(
        comment -> {
          try {
            thread
                .addComment(
                    Protos.Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(comment.getUpdatedAt().getTime())
                        .setCreatedBy(comment.getUser().getName())
                        .build())
                .setType(Protos.Thread.Type.DIFF);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
    return thread.build();
  }

  private List<Protos.Thread> getCodeThreads() {

    List<Protos.Thread> result = new ArrayList<>();
    List<String> comentedFiles =
        commentList
            .stream()
            .map(GHPullRequestReviewComment::getPath)
            .distinct()
            .collect(Collectors.toList());

    for (String fileName : comentedFiles) {
      List<Integer> lines =
          getCodeCommentsByFilename(fileName)
              .stream()
              .filter(comment -> comment.getPath().equals(fileName))
              .map(GHPullRequestReviewComment::getOriginalPosition)
              .distinct()
              .collect(Collectors.toList());
      lines.forEach(line -> result.add(getCodeThreadByFilenameAndLine(fileName, line)));
    }
    return result;
  }

  private List<GHPullRequestReviewComment> getCodeCommentsByFilename(String filename) {
    return commentList
        .stream()
        .filter(comment -> comment.getPath().equals(filename))
        .collect(Collectors.toList());
  }

  private Protos.Thread getCodeThreadByFilenameAndLine(String filename, int line) {

    List<GHPullRequestReviewComment> lineComments =
        getCodeCommentsByFilename(filename)
            .stream()
            .filter(comment -> comment.getOriginalPosition() == line)
            .collect(Collectors.toList());

    Protos.Thread.Builder thread = Protos.Thread.newBuilder();
    lineComments.forEach(
        comment -> {
          try {
            thread
                // .setRepoId()
                // .setCommitId()
                .setFile(
                    com.google.startupos.common.repo.Protos.File.newBuilder()
                        .setFilename(comment.getPath())
                        .setFilenameWithRepo(repoName + "/" + comment.getPath())
                        .setUser(pullRequest.getUser().getName())
                        // .setWorkspace()
                        // .setRepoId()
                        // .setCommitId()
                        // .setAction()
                        .build())
                .setLineNumber(line)
                // .setIsDone()
                .addComment(
                    Protos.Comment.newBuilder()
                        .setContent(comment.getBody())
                        .setTimestamp(comment.getUpdatedAt().getTime())
                        .setCreatedBy(comment.getUser().getName())
                        .build())
                .setType(Protos.Thread.Type.CODE);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    return thread.build();
  }
}

