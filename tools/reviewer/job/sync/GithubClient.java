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
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.UserRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.UserResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.IssueCommentsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestFilesRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestFilesResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateIssueCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreateReviewCommentRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.ReviewsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.ReviewsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.ReviewCommentsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.ReviewCommentsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CreatePullRequestReviewRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Allows sending GitHub API requests and returns responses. It requires a login and a password of
 * GitHub user.
 */
// TODO: Delete unused methods and constants
public class GithubClient {
  private static final String BASE_PATH = "https://api.github.com/";
  // https://developer.github.com/v3/pulls/#get-a-single-pull-request
  private static final String GET_PULL_REQUEST = "repos/%s/%s/pulls/%d";
  // https://developer.github.com/v3/users/#get-a-single-user
  private static final String GET_USER = "users/%s";
  // https://developer.github.com/v3/pulls/comments/#create-a-comment
  private static final String CREATE_REVIEW_COMMENT_ON_PULL_REQUEST =
      "repos/%s/%s/pulls/%d/comments";
  // https://developer.github.com/v3/pulls/comments/#list-comments-on-a-pull-request
  private static final String GET_REVIEW_COMMENTS_ON_PULL_REQUEST = "repos/%s/%s/pulls/%d/comments";
  // https://developer.github.com/v3/issues/comments/#create-a-comment
  private static final String CREATE_COMMENT_ON_ISSUE = "repos/%s/%s/issues/%d/comments";
  // https://developer.github.com/v3/issues/comments/#list-comments-on-an-issue
  private static final String GET_COMMENTS_ON_ISSUE = "repos/%s/%s/issues/%d/comments";
  // https://developer.github.com/v3/pulls/#create-a-pull-request
  private static final String CREATE_PULL_REQUEST = "repos/%s/%s/pulls";
  // https://developer.github.com/v3/pulls/#list-pull-requests
  private static final String GET_PULL_REQUESTS = "repos/%s/%s/pulls";
  // https://developer.github.com/v3/pulls/#list-pull-requests-files
  private static final String GET_PULL_REQUEST_FILES = "repos/%s/%s/pulls/%d/files";
  // https://developer.github.com/v3/pulls/reviews/#list-reviews-on-a-pull-request
  private static final String GET_PULL_REQUEST_REVIEWS = "repos/%s/%s/pulls/%d/reviews";
  // https://developer.github.com/v3/pulls/reviews/#get-comments-for-a-single-review
  private static final String GET_REVIEW_COMMENTS = "repos/%s/%s/pulls/%d/reviews/%d/comments";
  // https://developer.github.com/v3/pulls/reviews/#create-a-pull-request-review
  private static final String CREATE_PULL_REQUEST_REVIEW = "repos/%s/%s/pulls/%d/reviews";

  private final String login;
  private final String password;

  GithubClient(String login, String password) {
    this.login = login;
    this.password = password;
  }

  ReviewsResponse getReviews(ReviewsRequest request) throws IOException {
    ReviewsResponse.Builder builder = ReviewsResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_PULL_REQUEST_REVIEWS,
                request.getOwner(),
                request.getRepo(),
                request.getNumber()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"reviews\":%s}", response), builder);
    return builder.build();
  }

  ReviewCommentsResponse getReviewComments(ReviewCommentsRequest request) throws IOException {
    ReviewCommentsResponse.Builder builder = ReviewCommentsResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_REVIEW_COMMENTS,
                request.getOwner(),
                request.getRepo(),
                request.getNumber(),
                request.getReviewId()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"review_comments\":%s}", response), builder);
    return builder.build();
  }

  UserResponse getUser(UserRequest request) {
    UserResponse.Builder builder = UserResponse.newBuilder();
    String response = doRequest(RequestMethod.GET, String.format(GET_USER, request.getLogin()));
    try {
      JsonFormat.parser()
          .ignoringUnknownFields()
          .merge(String.format("{\"user\":%s}", response), builder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return builder.build();
  }

  //  CommentsResponse getReviewComments(CommentsRequest request) throws IOException {
  //    CommentsResponse.Builder builder = CommentsResponse.newBuilder();
  //    String response =
  //        doRequest(
  //            RequestMethod.GET,
  //            String.format(
  //                GET_REVIEW_COMMENTS_ON_PULL_REQUEST,
  //                request.getOwner(),
  //                request.getRepo(),
  //                request.getDiffNumber()));
  //    JsonFormat.parser()
  //        .ignoringUnknownFields()
  //        .merge(String.format("{\"comments\":%s}", response), builder);
  //    return builder.build();
  //  }

  IssueCommentsResponse getIssueComments(CommentsRequest request) throws IOException {
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

  PullRequestResponse getPullRequest(PullRequestRequest request) throws IOException {
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

  PullRequestsResponse getPullRequests(PullRequestsRequest request) throws IOException {
    PullRequestsResponse.Builder builder = PullRequestsResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(GET_PULL_REQUESTS, request.getOwner(), request.getRepo()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"pull_requests\":%s}", response), builder);
    return builder.build();
  }

  PullRequestFilesResponse getPullRequestFiles(PullRequestFilesRequest request) throws IOException {
    PullRequestFilesResponse.Builder builder = PullRequestFilesResponse.newBuilder();
    String response =
        doRequest(
            RequestMethod.GET,
            String.format(
                GET_PULL_REQUEST_FILES,
                request.getOwner(),
                request.getRepo(),
                request.getNumber()));
    JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(String.format("{\"files\":%s}", response), builder);
    return builder.build();
  }

  CreatePullRequestResponse createPullRequest(CreatePullRequestRequest request) {
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

  void createPullRequestReview(CreatePullRequestReviewRequest request) {
    try {
      String requestData =
          JsonFormat.printer().print(request.getRequestData()).replace("commitId", "commit_id");

      // GitHub API requires number format for number fields. E.g. we should change `position: "84"`
      // to `position: 84`
      Matcher matcher = Pattern.compile("\"\\d+\"").matcher(requestData);
      while (matcher.find()) {
        String numberWithDoubleQuotes = matcher.group();
        requestData =
            requestData.replaceFirst(
                numberWithDoubleQuotes,
                numberWithDoubleQuotes.substring(1, numberWithDoubleQuotes.length() - 1));
      }

      doRequest(
          RequestMethod.POST,
          String.format(
              CREATE_PULL_REQUEST_REVIEW,
              request.getOwner(),
              request.getRepo(),
              request.getNumber()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void createIssueComment(CreateIssueCommentRequest request) {
    try {
      String requestData = JsonFormat.printer().print(request.getRequestData());
      doRequest(
          RequestMethod.POST,
          String.format(
              CREATE_COMMENT_ON_ISSUE, request.getOwner(), request.getRepo(), request.getNumber()),
          requestData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void createReviewComment(CreateReviewCommentRequest request) {
    try {
      String requestData =
          JsonFormat.printer()
              .print(request.getRequestData())
              .replace("commitId", "commit_id")
              .replace("inReplyTo", "in_reply_to");

      Matcher matcher = Pattern.compile("\"\\d+\"").matcher(requestData);
      while (matcher.find()) {
        String numberWithDoubleQuotes = matcher.group();
        requestData =
            requestData.replaceFirst(
                numberWithDoubleQuotes,
                numberWithDoubleQuotes.substring(1, numberWithDoubleQuotes.length() - 1));
      }

      doRequest(
          RequestMethod.POST,
          String.format(
              CREATE_REVIEW_COMMENT_ON_PULL_REQUEST,
              request.getOwner(),
              request.getRepo(),
              request.getDiffNumber()),
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

      if (requestMethod.equals(RequestMethod.POST)) {
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestData.getBytes("UTF-8"));
      }

      if ((connection.getResponseCode() != HTTP_OK)
          && (connection.getResponseCode() != HTTP_CREATED)) {
        throw new IllegalStateException(connection.getResponseMessage());
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

