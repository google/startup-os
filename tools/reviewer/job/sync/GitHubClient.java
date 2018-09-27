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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class GitHubClient {

  private final ReviewerBot bot;

  GitHubClient(ReviewerBot bot) {
    this.bot = bot;
  }

  String postData(JSONObject json, String request) {
    try {
      URL url = new URL(request);
      StringBuilder response = new StringBuilder();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty(
          "Authorization",
          "Basic "
              + Base64.getEncoder()
                  .encodeToString((bot.getLogin() + ":" + bot.getPassword()).getBytes()));
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.getOutputStream().write(json.toString().getBytes("UTF-8"));

      if (connection.getResponseCode() != HTTP_CREATED) {
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

  String getResponse(String request) throws IOException {
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
      throw new IllegalStateException(connection.getResponseMessage());
    }
    return response.toString();
  }
}

