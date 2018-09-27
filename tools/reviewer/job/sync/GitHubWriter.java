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

import com.google.startupos.tools.reviewer.localserver.service.Protos.Comment;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Thread;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public class GitHubWriter {
  private final GitHubClient gitHubClient;

  GitHubWriter(GitHubClient gitHubClient) {
    this.gitHubClient = gitHubClient;
  }

  public void writeDiff(Diff diff, String repo) throws IOException {
    int prNumber = getPRNumber(diff, repo);

    for (Thread diffThread : diff.getDiffThreadList()) {
      for (Comment comment : diffThread.getCommentList()) {
        createPullRequestComment(
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + new Date(comment.getTimestamp())
                + "**\n"
                + comment.getContent(),
            prNumber);
      }
    }

    for (Thread codeThread : diff.getCodeThreadList()) {
      for (Comment comment : codeThread.getCommentList()) {
        String path = codeThread.getFile().getFilename();
        JSONArray files =
            new JSONArray(
                gitHubClient.getResponse(
                    String.format(
                        "https://api.github.com/repos/%s/pulls/%d/files", repo, prNumber)));
        int position = getCommentPosition(codeThread.getLineNumber(), getDiffHunk(files, path));
        createReviewComment(
            "Created by: **"
                + comment.getCreatedBy()
                + "**\nTime: **"
                + new Date(comment.getTimestamp())
                + "**\n"
                + comment.getContent(),
            codeThread.getCommitId(),
            path,
            position,
            prNumber);
      }
    }
  }

  private int getPRNumber(Diff diff, String repo) {
    try {
      JSONArray repos =
          new JSONArray(
              gitHubClient.getResponse(
                  String.format("https://api.github.com/repos/%s/pulls", repo)));
      for (Object item : repos) {
        JSONObject pr = (JSONObject) item;
        if (pr.getString("title").equals("D" + diff.getId())) {
          return pr.getInt("number");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Creating new PR if it isn't exist
    return createPullRequest(
        "D" + diff.getId(), "D" + diff.getId(), "master", diff.getDescription());
  }

  private int createPullRequest(String title, String head, String base, String body) {
    JSONObject json = new JSONObject();
    json.put("title", title);
    json.put("body", body);
    json.put("head", head);
    json.put("base", base);

    String request = "https://api.github.com/repos/val-fed/test-repo/pulls";
    JSONObject response = new JSONObject(gitHubClient.postData(json, request));
    return response.getInt("number");
  }

  private void createPullRequestComment(String body, int prNumber) {
    JSONObject json = new JSONObject();
    json.put("body", body);

    String request =
        String.format(
            "https://api.github.com/repos/val-fed/test-repo/issues/%d/comments", prNumber);
    gitHubClient.postData(json, request);
  }

  private String getDiffHunk(JSONArray files, String path) {
    for (Object item : files) {
      JSONObject file = (JSONObject) item;
      if (file.getString("filename").equals(path)) {
        return file.getString("patch");
      }
    }
    throw new RuntimeException("Diff hunk isn't found from file: " + path);
  }

  private Integer getCommentPosition(int lineNumber, String diffHunk) {
    String left = diffHunk.split(" ")[1].substring(1);
    String right = diffHunk.split(" ")[2].substring(1);
    boolean wasLeftEmpty = Integer.parseInt(left.split(",")[1]) == 0;
    // If the file is new we огіе return the "lineNumber".
    // Otherwise, we count the position by subtracting the position from which the "diff_hunk"
    // begins from "lineNumber" and adding 2.
    return wasLeftEmpty ? lineNumber : lineNumber - Integer.parseInt(right.split(",")[0]) + 2;
  }

  private void createReviewComment(
      String body, String commitId, String path, int position, int prNumber) {
    JSONObject json = new JSONObject();
    json.put("body", body);
    json.put("commit_id", commitId);
    json.put("path", path);
    json.put("position", position);

    String request =
        String.format("https://api.github.com/repos/val-fed/test-repo/pulls/%d/comments", prNumber);
    gitHubClient.postData(json, request);
  }
}

