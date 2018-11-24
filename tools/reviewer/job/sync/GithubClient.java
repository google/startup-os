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

import com.google.protobuf.util.JsonFormat;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateIssueCommentResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.DeleteIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.DeleteReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.EditIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.EditReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.IssueCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.IssueCommentsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.RepositoryCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.RepositoryCommentsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Allows sending GitHub API requests and returns responses. It requires a login and a password of
 * GitHub user.
 */
public class GithubClient {
  private static final String BASE_PATH = "https://api.github.com/";
  // https://developer.github.com/v3/pulls/#get-a-single-pull-request
  private static final String GET_PULL_REQUEST = "repos/%s/%s/pulls/%d";
  // https://developer.github.com/v3/pulls/#create-a-pull-request
  private static final String CREATE_PULL_REQUEST = "repos/%s/%s/pulls";
  // https://developer.github.com/v3/issues/comments/#list-comments-on-an-issue
  private static final String GET_COMMENTS_ON_ISSUE = "repos/%s/%s/issues/%d/comments";
  // https://developer.github.com/v3/issues/comments/#create-a-comment
  private static final String CREATE_COMMENT_ON_ISSUE = "repos/%s/%s/issues/%d/comments";
  // https://developer.github.com/v3/pulls/comments/#create-a-comment
  private static final String CREATE_COMMENT_ON_REVIEW = "repos/%s/%s/pulls/%d/comments";
  // https://developer.github.com/v3/repos/commits/#get-a-single-commit
  private static final String GET_COMMIT = "repos/%s/%s/commits/%s";
  // https://developer.github.com/v3/pulls/#list-commits-on-a-pull-request
  private static final String GET_COMMITS_ON_PULL_REQUEST = "repos/%s/%s/pulls/%s/commits";
  // https://developer.github.com/v3/pulls/comments/#list-comments-in-a-repository
  private static final String GET_REPOSITORY_COMMENTS = "repos/%s/%s/pulls/comments";
  // https://developer.github.com/v3/pulls/comments/#edit-a-comment
  private static final String EDIT_REVIEW_COMMENT = "repos/%s/%s/pulls/comments/%s";
  // https://developer.github.com/v3/pulls/comments/#delete-a-comment
  private static final String DELETE_REVIEW_COMMENT = "repos/%s/%s/pulls/comments/%s";
  // https://developer.github.com/v3/issues/comments/#edit-a-comment
  private static final String EDIT_ISSUE_COMMENT = "repos/%s/%s/issues/comments/%s";
  // https://developer.github.com/v3/issues/comments/#delete-a-comment
  private static final String DELETE_ISSUE_COMMENT = "repos/%s/%s/issues/comments/%s";

  private final String login;
  private final String password;

  GithubClient(String login, String password) {
    this.login = login;
    this.password = password;
  }

  public PullRequestResponse getPullRequest(PullRequestRequest request) throws IOException {
    PullRequestResponse.Builder builder = PullRequestResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_PULL_REQUEST, request.getOwner(), request.getRepo(), request.getNumber()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"pull_request\":%s}", response), builder);
    return builder.build();
  }

  public CreatePullRequestResponse createPullRequest(CreatePullRequestRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      String response =
          doRequest(
              RequestMethod.POST,
              String.format(CREATE_PULL_REQUEST, request.getOwner(), request.getRepo()),
              requestData);
      CreatePullRequestResponse.Builder builder = CreatePullRequestResponse.newBuilder();
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(String.format("{\"pull_request\":%s}", response), builder);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public IssueCommentsResponse getIssueComments(IssueCommentsRequest request) throws IOException {
    IssueCommentsResponse.Builder builder = IssueCommentsResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_COMMENTS_ON_ISSUE, request.getOwner(), request.getRepo(), request.getNumber()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"issue_comment\":%s}", response), builder);
    return builder.build();
  }

  public RepositoryCommentsResponse getRepositoryComments(RepositoryCommentsRequest request)
      throws IOException {
    RepositoryCommentsResponse.Builder builder = RepositoryCommentsResponse.newBuilder();
    String response =
        doRequest(
                RequestMethod.GET,
                String.format(GET_REPOSITORY_COMMENTS, request.getOwner(), request.getRepo()))
            .replace("_links", "link")
            .replace("href", "url");
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"review_comment\":%s}", response), builder);
    return builder.build();
  }

  public CreateIssueCommentResponse createIssueComment(CreateIssueCommentRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      String response =
          doRequest(
              RequestMethod.POST,
              String.format(
                  CREATE_COMMENT_ON_ISSUE,
                  request.getOwner(),
                  request.getRepo(),
                  request.getNumber()),
              requestData);
      CreateIssueCommentResponse.Builder builder = CreateIssueCommentResponse.newBuilder();
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(String.format("{\"issue_comment\":%s}", response), builder);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void editIssueComment(EditIssueCommentRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      doRequest(
          RequestMethod.PATCH,
          String.format(
              EDIT_ISSUE_COMMENT, request.getOwner(), request.getRepo(), request.getCommentId()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void deleteIssueComment(DeleteIssueCommentRequest request) {
    doRequest(
        RequestMethod.DELETE,
        String.format(
            DELETE_ISSUE_COMMENT, request.getOwner(), request.getRepo(), request.getCommentId()));
  }

  public CreateReviewCommentResponse createReviewComment(CreateReviewCommentRequest request) {
    try {
      String requestData =
          JsonFormat.printer().print(request.getRequestData()).replace("commitId", "commit_id");
      String response =
          doRequest(
              RequestMethod.POST,
              String.format(
                  CREATE_COMMENT_ON_REVIEW,
                  request.getOwner(),
                  request.getRepo(),
                  request.getNumber()),
              requestData);
      CreateReviewCommentResponse.Builder builder = CreateReviewCommentResponse.newBuilder();
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(String.format("{\"review_comment\":%s}", response), builder);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void editReviewComment(EditReviewCommentRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      doRequest(
          RequestMethod.PATCH,
          String.format(
              EDIT_REVIEW_COMMENT, request.getOwner(), request.getRepo(), request.getCommentId()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void deleteReviewComment(DeleteReviewCommentRequest request) {
    doRequest(
        RequestMethod.DELETE,
        String.format(
            DELETE_REVIEW_COMMENT, request.getOwner(), request.getRepo(), request.getCommentId()));
  }

  public CommitResponse getCommit(CommitRequest request) throws IOException {
    CommitResponse.Builder builder = CommitResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(GET_COMMIT, request.getOwner(), request.getRepo(), request.getSha()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"commit\":%s}", response), builder);
    return builder.build();
  }

  public CommitsResponse getCommits(CommitsRequest request) throws IOException {
    CommitsResponse.Builder builder = CommitsResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_COMMITS_ON_PULL_REQUEST,
                request.getOwner(),
                request.getRepo(),
                request.getNumber()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"commits\":%s}", response), builder);
    return builder.build();
  }

  private String doRequest(RequestMethod requestMethod, String request) {
    if (requestMethod.equals(RequestMethod.GET) || requestMethod.equals(RequestMethod.DELETE)) {
      return doRequest(requestMethod, request, "");
    } else {
      throw new RuntimeException(requestMethod.name() + " method requires the request data.");
    }
  }

  private String doRequest(RequestMethod requestMethod, String request, String requestData) {
    try {
      URL url = new URL(BASE_PATH + request);
      StringBuilder response = new StringBuilder();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      if (requestMethod.equals(RequestMethod.PATCH)) {
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        connection.setRequestMethod("POST");
      } else {
        connection.setRequestMethod(requestMethod.name());
      }
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty(
          "Authorization",
          "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes()));

      if (requestMethod.equals(RequestMethod.POST) || requestMethod.equals(RequestMethod.PATCH)) {
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestData.getBytes("UTF-8"));
      }

      if ((connection.getResponseCode() != HTTP_OK)
          && (connection.getResponseCode() != HTTP_CREATED)
          && (connection.getResponseCode() != HTTP_NO_CONTENT)) {
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
          String line;
          while ((line = reader.readLine()) != null) {
            errorResponse.append(line);
          }
        }
        throw new IllegalStateException(
            "Error response: "
                + errorResponse
                + "\nResponse code: "
                + connection.getResponseCode()
                + ".\n"
                + connection.getResponseMessage());
      }

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
      }
      return response.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private enum RequestMethod {
    GET,
    POST,
    PATCH,
    DELETE
  }
}

