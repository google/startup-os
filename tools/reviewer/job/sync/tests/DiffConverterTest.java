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

package com.google.startupos.tools.reviewer.job.sync.tests;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.reviewer.job.sync.DiffConverter;
import com.google.startupos.tools.reviewer.job.sync.GithubClient;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitsRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.CommitsResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubProtos.PullRequestResponse;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.CommitInfo;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.CommitPointer;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.PullRequest;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos.User;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiffConverterTest {

  private GithubClient githubClient = mock(GithubClient.class);
  private FileUtils fileUtils;
  private static final String FEATURE_BRANCH_NAME = "D123";
  private static final User AUTHOR_USER =
      User.newBuilder().setEmail("test.author@test.com").build();
  private static final User REVIEWER_USER =
      User.newBuilder().setEmail("test.reviewer@test.com").build();
  private static final User BASE_USER =
      User.newBuilder().setEmail("test.base.user@test.com").build();

  private static final String REPO_OWNER = "test-owner";
  private static final String REPO_NAME = "test-repo";
  private static final Integer PULL_REQUEST_NUMBER = 120;

  private static final CommitPointer BASE =
      CommitPointer.newBuilder().setSha("base_sha").setRef("master").setUser(BASE_USER).build();
  private static final CommitPointer HEAD =
      CommitPointer.newBuilder()
          .setSha("head_sha")
          .setRef(FEATURE_BRANCH_NAME)
          .setUser(AUTHOR_USER)
          .build();

  private static final String REPO_OWNER_2 = "test-owner2";
  private static final String REPO_NAME_2 = "test-repo2";
  private static final Integer PULL_REQUEST_NUMBER_2 = 220;
  private static final CommitPointer BASE_2 =
      CommitPointer.newBuilder().setSha("base_sha2").setRef("master").setUser(BASE_USER).build();
  private static final CommitPointer HEAD_2 =
      CommitPointer.newBuilder()
          .setSha("head_sha2")
          .setRef(FEATURE_BRANCH_NAME)
          .setUser(AUTHOR_USER)
          .build();

  @Singleton
  @Component(modules = {CommonModule.class})
  interface TestComponent {
    FileUtils getFileUtils();
  }

  @Before
  public void setup() throws IOException {
    TestComponent component = DaggerDiffConverterTest_TestComponent.builder().build();
    fileUtils = component.getFileUtils();
    mockGitHubClientMethods();
  }

  @Test
  public void diffConverterTest() throws IOException {
    Diff diff =
        (Diff)
            fileUtils.readPrototxtUnchecked(
                "tools/reviewer/job/sync/tests/resources/diff.prototxt", Diff.newBuilder());
    DiffConverter diffConverter = new DiffConverter(githubClient);

    List<PullRequest> expectedPullRequest =
        Arrays.asList(
            (PullRequest)
                fileUtils.readPrototxtUnchecked(
                    "tools/reviewer/job/sync/tests/resources/pull_request.prototxt",
                    PullRequest.newBuilder()),
            (PullRequest)
                fileUtils.readPrototxtUnchecked(
                    "tools/reviewer/job/sync/tests/resources/pull_request_2.prototxt",
                    PullRequest.newBuilder()));

    assertEquals(expectedPullRequest, diffConverter.convertDiffToPullRequests(diff));
  }

  private void mockGitHubClientMethods() throws IOException {
    when(githubClient.getPullRequest(
            PullRequestRequest.newBuilder()
                .setOwner(REPO_OWNER)
                .setRepo(REPO_NAME)
                .setNumber(PULL_REQUEST_NUMBER)
                .build()))
        .thenReturn(
            PullRequestResponse.newBuilder()
                .setPullRequest(PullRequest.newBuilder().setBase(BASE).setHead(HEAD).build())
                .build());

    when(githubClient.getPullRequest(
            PullRequestRequest.newBuilder()
                .setOwner(REPO_OWNER_2)
                .setRepo(REPO_NAME_2)
                .setNumber(PULL_REQUEST_NUMBER_2)
                .build()))
        .thenReturn(
            PullRequestResponse.newBuilder()
                .setPullRequest(PullRequest.newBuilder().setBase(BASE_2).setHead(HEAD_2).build())
                .build());

    when(githubClient.getCommit(
            CommitRequest.newBuilder()
                .setOwner(REPO_OWNER)
                .setRepo(REPO_NAME)
                .setSha(HEAD.getSha())
                .build()))
        .thenReturn(
            CommitResponse.newBuilder()
                .setCommit(
                    CommitInfo.newBuilder()
                        .setSha(HEAD.getSha())
                        .setCommit(
                            CommitInfo.Commit.newBuilder()
                                .setAuthor(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("author_date")
                                        .build())
                                .setCommitter(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("committer_date")
                                        .build())
                                .setMessage("commit message")
                                .setTree(CommitInfo.Tree.newBuilder().setSha(HEAD.getSha()).build())
                                .build())
                        .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .setCommitter(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .addAllParents(
                            Arrays.asList(
                                CommitInfo.Tree.newBuilder().setSha(BASE.getSha()).build()))
                        .addAllFiles(
                            Arrays.asList(
                                CommitInfo.File.newBuilder()
                                    .setFilename("first_file.txt")
                                    .setAdditions(4)
                                    .setDeletions(3)
                                    .setChanges(7)
                                    .setStatus("modified")
                                    .setPatch(
                                        "@@ -7,13 +7,14 @@ line6\n line7\n line8\n line9\n-line10\n-line11\n-line12\n+CHANGED_line10\n+CHANGED_line11\n+CHANGED_line12\n line13\n line14\n line15\n line16\n+ADDED_line#1\n line17\n line18\n line19")
                                    .build(),
                                CommitInfo.File.newBuilder()
                                    .setFilename("second_file.txt")
                                    .setAdditions(7)
                                    .setDeletions(2)
                                    .setChanges(9)
                                    .setStatus("modified")
                                    .setPatch(
                                        "@@ -1,5 +1,5 @@\n line1\n-line2\n+CHANGED_line2\n line3\n line4\n line5\n@@ -12,9 +12,14 @@ line11\n line12\n line13\n line14\n-line15\n+CHANGED_line15\n line16\n line17\n line18\n line19\n line20\n+ADDED_line1\n+ADDED_line2\n+ADDED_line3\n+ADDED_line4\n+ADDED_line5")
                                    .build()))
                        .build())
                .build());

    when(githubClient.getCommit(
            CommitRequest.newBuilder()
                .setOwner(REPO_OWNER_2)
                .setRepo(REPO_NAME_2)
                .setSha(HEAD_2.getSha())
                .build()))
        .thenReturn(
            CommitResponse.newBuilder()
                .setCommit(
                    CommitInfo.newBuilder()
                        .setSha(HEAD_2.getSha())
                        .setCommit(
                            CommitInfo.Commit.newBuilder()
                                .setAuthor(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("author_date")
                                        .build())
                                .setCommitter(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("committer_date")
                                        .build())
                                .setMessage("commit message")
                                .setTree(
                                    CommitInfo.Tree.newBuilder().setSha(HEAD_2.getSha()).build())
                                .build())
                        .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .setCommitter(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .addAllParents(
                            Arrays.asList(
                                CommitInfo.Tree.newBuilder().setSha(BASE_2.getSha()).build()))
                        .addAllFiles(
                            Arrays.asList(
                                CommitInfo.File.newBuilder()
                                    .setFilename("repo2_file.txt")
                                    .setAdditions(2)
                                    .setDeletions(1)
                                    .setChanges(3)
                                    .setStatus("modified")
                                    .setPatch(
                                        "@@ -3,9 +3,10 @@ line2\n line3\n line4\n line5\n+\n line6\n line7\n line8\n line9\n-line10\n+\tline10\n ")
                                    .build()))
                        .build())
                .build());

    when(githubClient.getCommit(
            CommitRequest.newBuilder()
                .setOwner(REPO_OWNER)
                .setRepo(REPO_NAME)
                .setSha(BASE.getSha())
                .build()))
        .thenReturn(
            CommitResponse.newBuilder()
                .setCommit(
                    CommitInfo.newBuilder()
                        .setSha(BASE.getSha())
                        .setCommit(
                            CommitInfo.Commit.newBuilder()
                                .setAuthor(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("author_date")
                                        .build())
                                .setCommitter(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("committer_date")
                                        .build())
                                .setMessage("commit message")
                                .setTree(CommitInfo.Tree.newBuilder().setSha(HEAD.getSha()).build())
                                .build())
                        .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .setCommitter(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .addAllParents(
                            Arrays.asList(
                                CommitInfo.Tree.newBuilder().setSha("base_parent_sha").build()))
                        .addAllFiles(
                            Arrays.asList(
                                CommitInfo.File.newBuilder()
                                    .setFilename("first_file.txt")
                                    .setAdditions(20)
                                    .setDeletions(0)
                                    .setChanges(20)
                                    .setStatus("added")
                                    .setPatch(
                                        "@@ -0,0 +1,20 @@\n+line1\n+line2\n+line3\n+line4\n+line5\n+line6\n+line7\n+line8\n+line9\n+line10\n+line11\n+line12\n+line13\n+line14\n+line15\n+line16\n+line17\n+line18\n+line19\n+line20")
                                    .build(),
                                CommitInfo.File.newBuilder()
                                    .setFilename("second_file.txt")
                                    .setAdditions(20)
                                    .setDeletions(0)
                                    .setChanges(20)
                                    .setStatus("added")
                                    .setPatch(
                                        "@@ -0,0 +1,20 @@\n+line1\n+line2\n+line3\n+line4\n+line5\n+line6\n+line7\n+line8\n+line9\n+line10\n+line11\n+line12\n+line13\n+line14\n+line15\n+line16\n+line17\n+line18\n+line19\n+line20")
                                    .build()))
                        .build())
                .build());

    when(githubClient.getCommit(
            CommitRequest.newBuilder()
                .setOwner(REPO_OWNER_2)
                .setRepo(REPO_NAME_2)
                .setSha(BASE_2.getSha())
                .build()))
        .thenReturn(
            CommitResponse.newBuilder()
                .setCommit(
                    CommitInfo.newBuilder()
                        .setSha(BASE_2.getSha())
                        .setCommit(
                            CommitInfo.Commit.newBuilder()
                                .setAuthor(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("author_date")
                                        .build())
                                .setCommitter(
                                    CommitInfo.Commit.User.newBuilder()
                                        .setName("author_name")
                                        .setEmail(AUTHOR_USER.getEmail())
                                        .setDate("committer_date")
                                        .build())
                                .setMessage("commit message")
                                .setTree(
                                    CommitInfo.Tree.newBuilder().setSha(HEAD_2.getSha()).build())
                                .build())
                        .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .setCommitter(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                        .addAllParents(
                            Arrays.asList(
                                CommitInfo.Tree.newBuilder().setSha("base_parent_sha").build()))
                        .addAllFiles(
                            Arrays.asList(
                                CommitInfo.File.newBuilder()
                                    .setFilename("repo2_file.txt")
                                    .setAdditions(5)
                                    .setDeletions(0)
                                    .setChanges(5)
                                    .setStatus("modified")
                                    .setPatch(
                                        "@@ -3,4 +3,9 @@ line2\n line3\n line4\n line5\n+line6\n+line7\n+line8\n+line9\n+line10\n ")
                                    .build()))
                        .build())
                .build());

    when(githubClient.getCommits(
            CommitsRequest.newBuilder()
                .setOwner(REPO_OWNER)
                .setRepo(REPO_NAME)
                .setNumber(PULL_REQUEST_NUMBER)
                .build()))
        .thenReturn(
            CommitsResponse.newBuilder()
                .addAllCommits(
                    Arrays.asList(
                        CommitInfo.newBuilder()
                            .setSha(HEAD.getSha())
                            .setCommit(
                                CommitInfo.Commit.newBuilder()
                                    .setAuthor(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("author_date")
                                            .build())
                                    .setCommitter(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("commiter_date")
                                            .build())
                                    .setMessage("commit message")
                                    .setTree(
                                        CommitInfo.Tree.newBuilder().setSha(HEAD.getSha()).build())
                                    .build())
                            .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .setCommitter(
                                User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .addAllParents(
                                Arrays.asList(
                                    CommitInfo.Tree.newBuilder().setSha(BASE.getSha()).build()))
                            .build(),
                        CommitInfo.newBuilder()
                            .setSha(BASE.getSha())
                            .setCommit(
                                CommitInfo.Commit.newBuilder()
                                    .setAuthor(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("author_date")
                                            .build())
                                    .setCommitter(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("commiter_date")
                                            .build())
                                    .setMessage("commit message")
                                    .setTree(
                                        CommitInfo.Tree.newBuilder().setSha(BASE.getSha()).build())
                                    .build())
                            .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .setCommitter(
                                User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .addAllParents(
                                Arrays.asList(
                                    CommitInfo.Tree.newBuilder()
                                        .setSha("parent_of_base_sha")
                                        .build()))
                            .build()))
                .build());

    when(githubClient.getCommits(
            CommitsRequest.newBuilder()
                .setOwner(REPO_OWNER_2)
                .setRepo(REPO_NAME_2)
                .setNumber(PULL_REQUEST_NUMBER_2)
                .build()))
        .thenReturn(
            CommitsResponse.newBuilder()
                .addAllCommits(
                    Arrays.asList(
                        CommitInfo.newBuilder()
                            .setSha(HEAD_2.getSha())
                            .setCommit(
                                CommitInfo.Commit.newBuilder()
                                    .setAuthor(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("author_date")
                                            .build())
                                    .setCommitter(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("commiter_date")
                                            .build())
                                    .setMessage("commit message")
                                    .setTree(
                                        CommitInfo.Tree.newBuilder()
                                            .setSha(HEAD_2.getSha())
                                            .build())
                                    .build())
                            .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .setCommitter(
                                User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .addAllParents(
                                Arrays.asList(
                                    CommitInfo.Tree.newBuilder().setSha(BASE_2.getSha()).build()))
                            .build(),
                        CommitInfo.newBuilder()
                            .setSha(BASE_2.getSha())
                            .setCommit(
                                CommitInfo.Commit.newBuilder()
                                    .setAuthor(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("author_date")
                                            .build())
                                    .setCommitter(
                                        CommitInfo.Commit.User.newBuilder()
                                            .setName("author-name")
                                            .setEmail(AUTHOR_USER.getEmail())
                                            .setDate("commiter_date")
                                            .build())
                                    .setMessage("commit message")
                                    .setTree(
                                        CommitInfo.Tree.newBuilder()
                                            .setSha(BASE_2.getSha())
                                            .build())
                                    .build())
                            .setAuthor(User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .setCommitter(
                                User.newBuilder().setEmail(AUTHOR_USER.getEmail()).build())
                            .addAllParents(
                                Arrays.asList(
                                    CommitInfo.Tree.newBuilder()
                                        .setSha("parent_of_base_sha")
                                        .build()))
                            .build()))
                .build());
  }
}

