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
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_OK;

public class GitHubReader {
  private static final String PR_REQUEST = "https://api.github.com/repos/%s/pulls/%d";
  private static final String USER_EMAIL_REQUEST = "https://api.github.com/users/%s";
  private static final String PR_CODE_COMMENTS_REQUEST =
      "https://api.github.com/repos/%s/pulls/%d/comments";
  private static final String PR_COMMENTS_REQUEST =
      "https://api.github.com/repos/%s/issues/%d/comments";

  private final ReviewerBot bot;

  GitHubReader(ReviewerBot bot) {
    this.bot = bot;
  }

  private class PullRequest {
    private long diffId;
    private String repoId;
    private String description;
    private long created_timestamp;
    private long modified_timestamp;
    private String prAuthorLogin;
  }

  private PullRequest processPullRequest(String response) {
    JSONObject pullRequest = new JSONObject(response);
    PullRequest pr = new PullRequest();
    pr.diffId =
        Long.parseLong(pullRequest.getJSONObject("head").getString("ref").replaceFirst("D", ""));
    pr.description = pullRequest.getString("body");
    pr.created_timestamp = Instant.parse(pullRequest.getString("created_at")).getEpochSecond();
    pr.modified_timestamp = Instant.parse(pullRequest.getString("updated_at")).getEpochSecond();
    pr.repoId = pullRequest.getJSONObject("head").getJSONObject("repo").getString("name");
    // TODO: Change prAuthorLogin if PR will be created by ReviewerBot
    pr.prAuthorLogin = pullRequest.getJSONObject("user").getString("login");
    return pr;
  }

  public Diff getDiff(String repo, int diffNumber) throws IOException {
    String prResponse = getResponse(String.format(PR_REQUEST, repo, diffNumber), bot);
    PullRequest pr = processPullRequest(prResponse);
    return Diff.newBuilder()
        .setId(pr.diffId)
        .setAuthor(
            Author.newBuilder()
                .setEmail(getUserEmail(String.format(USER_EMAIL_REQUEST, pr.prAuthorLogin)))
                .build())
        .setDescription(pr.description)
        .setCreatedTimestamp(pr.created_timestamp)
        .setModifiedTimestamp(pr.modified_timestamp)
        .addAllCodeThread(
            getCodeThreads(pr, String.format(PR_CODE_COMMENTS_REQUEST, repo, diffNumber)))
        .addDiffThread(getDiffThread(String.format(PR_COMMENTS_REQUEST, repo, diffNumber)))
        .build();
  }

  private ImmutableList<Thread> getCodeThreads(PullRequest pr, String request) throws IOException {
    List<Thread> result = new ArrayList<>();
    JSONArray response = new JSONArray(getResponse(request, bot));
    List<String> commentedFiles = new ArrayList<>();
    List<JSONObject> codeComments = new ArrayList<>();
    for (Object item : response) {
      JSONObject comment = (JSONObject) item;
      commentedFiles.add(comment.getString("path"));
      codeComments.add(comment);
    }
    commentedFiles = commentedFiles.stream().distinct().collect(Collectors.toList());

    for (String fileName : commentedFiles) {
      ImmutableList<JSONObject> fileComments =
          getCodeCommentsByFilename(codeComments, fileName);
      List<Integer> lines =
          fileComments
              .stream()
              .filter(comment -> comment.getString("path").equals(fileName))
              .map(
                  comment ->
                      getLineNumber(
                          comment.getString("diff_hunk"), comment.getInt("original_position")))
              .distinct()
              .collect(Collectors.toList());

      lines.forEach(line -> result.add(getCodeThreadByFilenameAndLine(fileComments, line, pr)));
    }
    return ImmutableList.copyOf(result);
  }

  private ImmutableList<JSONObject> getCodeCommentsByFilename(
      List<JSONObject> codeComments, String filename) {
    return ImmutableList.copyOf(
        codeComments
            .stream()
            .filter(comment -> comment.getString("path").equals(filename))
            .collect(Collectors.toList()));
  }

  private Thread getCodeThreadByFilenameAndLine(
      ImmutableList<JSONObject> fileComments, int line, PullRequest pr) {
    ImmutableList<JSONObject> lineComments =
        ImmutableList.copyOf(
            fileComments
                .stream()
                .filter(
                    item ->
                        getLineNumber(item.getString("diff_hunk"), item.getInt("original_position"))
                            == line)
                .collect(Collectors.toList()));

    Thread.Builder thread = Thread.newBuilder();
    lineComments.forEach(
        comment ->
            thread
                .setRepoId(pr.repoId)
                .setCommitId(comment.getString("original_commit_id"))
                .setFile(
                    Protos.File.newBuilder()
                        .setFilename(comment.getString("path"))
                        .setRepoId(pr.repoId)
                        .setFilenameWithRepo(pr.repoId + "/" + comment.getString("path"))
                        .setUser(getUserEmail(String.format(USER_EMAIL_REQUEST, pr.prAuthorLogin)))
                        .build())
                .setLineNumber(
                    getLineNumber(
                        comment.getString("diff_hunk"), comment.getInt("original_position")))
                .addComment(
                    Comment.newBuilder()
                        .setContent(comment.getString("body"))
                        .setTimestamp(
                            Instant.parse(comment.getString("updated_at")).getEpochSecond())
                        .setCreatedBy(
                            // TODO: Email can be null if the user hides email. Think over how to
                            // resolve this.
                            getUserEmail(
                                String.format(
                                    USER_EMAIL_REQUEST,
                                    comment.getJSONObject("user").getString("login"))))
                        .build())
                .setType(Thread.Type.CODE));
    return thread.build();
  }

  private Integer getLineNumber(String diffHunk, int position) {
    String left = diffHunk.split(" ")[1].substring(1);
    String right = diffHunk.split(" ")[2].substring(1);
    boolean wasLeftEmpty = Integer.parseInt(left.split(",")[1]) == 0;
    // If the file is new we огіе return the "position".
    // Otherwise, we count the position by adding 3 to the position from which the "diff_hunk"
    // begins.
    return wasLeftEmpty ? position : Integer.parseInt(right.split(",")[0]) + 3;
  }

  private Thread getDiffThread(String request) throws IOException {
    Thread.Builder thread = Thread.newBuilder();
    JSONArray response = new JSONArray(getResponse(request, bot));
    response.forEach(
        item -> {
          JSONObject comment = (JSONObject) item;
          thread
              .addComment(
                  Comment.newBuilder()
                      .setContent(comment.getString("body"))
                      .setTimestamp(Instant.parse(comment.getString("updated_at")).getEpochSecond())
                      .setCreatedBy(
                          getUserEmail(
                              String.format(
                                  USER_EMAIL_REQUEST,
                                  comment.getJSONObject("user").getString("login"))))
                      .build())
              .setType(Thread.Type.DIFF);
        });
    return thread.build();
  }

  private String getUserEmail(String request) {
    try {
      return new JSONObject(getResponse(request, bot)).get("email").toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getResponse(String request, ReviewerBot bot) throws IOException {
    StringBuilder response = new StringBuilder();
    URL url = new URL(request);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(
        "Authorization",
        "Basic "
            + Base64.getEncoder()
                .encodeToString((bot.getLogin() + ":" + bot.getPassword()).getBytes()));
    connection.setRequestMethod("GET");
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    }
    if (connection.getResponseCode() != HTTP_OK) {
      throw new IllegalStateException("getDocument failed: " + connection.getResponseMessage());
    }
    return response.toString();
  }
}

