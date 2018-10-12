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
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.UserRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.UserResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CommentsResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestFilesRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestFilesResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestsResponse;
import com.google.startupos.tools.reviewer.job.sync.GitHubProtos.PullRequestsRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class GitHubClient {
  private static final String BASE_PATH = "https://api.github.com/";
  private static final String GET_SINGLE_PULL_REQUEST = "repos/%s/pulls/%d";
  private static final String GET_SINGLE_USER = "users/%s";
  private static final String CREATE_REVIEW_COMMENT_ON_PULL_REQUEST = "repos/%s/pulls/%d/comments";
  private static final String GET_LIST_REVIEW_COMMENTS_ON_PULL_REQUEST =
      "repos/%s/pulls/%d/comments";
  private static final String CREATE_COMMENT_ON_ISSUE = "repos/%s/issues/%d/comments";
  private static final String GET_LIST_COMMENTS_ON_ISSUE = "repos/%s/issues/%d/comments";
  private static final String CREATE_PR_OR_GET_LIST_PR = "repos/%s/pulls";
  private static final String GET_PR_FILES_LIST = "repos/%s/pulls/%d/files";

  private final String login;
  private final String password;

  GitHubClient(String login, String password) {
    this.login = login;
    this.password = password;
  }

  UserResponse getUser(UserRequest request) {
    UserResponse.Builder builder = UserResponse.newBuilder();
    try {
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(
              "{\"user\":"
                  + doRequest(RequestMethod.GET, String.format(GET_SINGLE_USER, request.getLogin()))
                  + "}",
              builder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  CommentsResponse getReviewComments(CommentsRequest request) throws IOException {
    CommentsResponse.Builder builder = CommentsResponse.newBuilder();
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(
            "{\"comments\":"
                + doRequest(
                    RequestMethod.GET,
                    String.format(
                        GET_LIST_REVIEW_COMMENTS_ON_PULL_REQUEST,
                        request.getRepo(),
                        request.getDiffNumber()))
                + "}",
            builder);
    return builder.build();
  }

  CommentsResponse getIssueComments(CommentsRequest request) throws IOException {
    CommentsResponse.Builder builder = CommentsResponse.newBuilder();
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(
            "{\"comments\":"
                + doRequest(
                    RequestMethod.GET,
                    String.format(
                        GET_LIST_COMMENTS_ON_ISSUE, request.getRepo(), request.getDiffNumber()))
                + "}",
            builder);
    return builder.build();
  }

  PullRequestResponse getPullRequest(PullRequestRequest request) throws IOException {
    PullRequestResponse.Builder builder = PullRequestResponse.newBuilder();
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(
            "{\"pull_request\":"
                + doRequest(
                    RequestMethod.GET,
                    String.format(
                        GET_SINGLE_PULL_REQUEST, request.getRepo(), request.getDiffNumber()))
                + "}",
            builder);
    return builder.build();
  }

  PullRequestsResponse getPullRequests(PullRequestsRequest request) throws IOException {
    PullRequestsResponse.Builder builder = PullRequestsResponse.newBuilder();
    String response =
        doRequest(RequestMethod.GET, String.format(CREATE_PR_OR_GET_LIST_PR, request.getRepo()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge("{\"pull_requests\":" + response + "}", builder);
    return builder.build();
  }

  PullRequestFilesResponse getPullRequestFiles(PullRequestFilesRequest request) throws IOException {
    PullRequestFilesResponse.Builder builder = PullRequestFilesResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(GET_PR_FILES_LIST, request.getRepo(), request.getDiffNumber()));
    JsonFormat.parser().ignoringUnknownFields().merge("{\"files\":" + response + "}", builder);
    return builder.build();
  }

  CreatePullRequestResponse createPullRequest(CreatePullRequestRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      String response =
          "{\"pull_request\":"
              + doRequest(
                  RequestMethod.POST,
                  String.format(CREATE_PR_OR_GET_LIST_PR, request.getRepo()),
                  requestData)
              + "}";
      CreatePullRequestResponse.Builder builder = CreatePullRequestResponse.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(response, builder);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void createPullRequestComment(CreatePullRequestCommentRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      doRequest(
          RequestMethod.POST,
          String.format(CREATE_COMMENT_ON_ISSUE, request.getRepo(), request.getDiffNumber()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void createReviewComment(CreateReviewCommentRequest request) {
    try {
      String requestData =
          JsonFormat.printer().print(request.getRequestData()).replace("commitId", "commit_id");
      doRequest(
          RequestMethod.POST,
          String.format(
              CREATE_REVIEW_COMMENT_ON_PULL_REQUEST, request.getRepo(), request.getDiffNumber()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String doRequest(RequestMethod requestMethod, String request) {
    if (requestMethod.equals(RequestMethod.GET)) {
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
      connection.setRequestMethod(requestMethod.name());
      connection.setRequestProperty(
          "Authorization",
          "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes()));
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

      if (requestMethod.name().equals("POST")) {
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestData.getBytes("UTF-8"));

        if (connection.getResponseCode() != HTTP_CREATED) {
          throw new IllegalStateException(connection.getResponseMessage());
        }
      } else {
        if (connection.getResponseCode() != HTTP_OK) {
          throw new IllegalStateException(connection.getResponseMessage());
        }
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
    POST
  }
}

