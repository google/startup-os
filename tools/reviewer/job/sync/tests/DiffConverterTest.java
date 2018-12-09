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

import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.tools.reviewer.job.sync.DiffConverter;
import com.google.startupos.tools.reviewer.job.sync.GithubPullRequestProtos;
import com.google.startupos.tools.reviewer.localserver.service.Protos;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiffConverterTest {
  private static final String TEST_FILE_PATCH =
      "@@ -2,7 +2,7 @@ line-1\n"
          + " line-2\n"
          + " line-3\n"
          + " line-4\n"
          + "-line-5\n"
          + "+(CHANGED)line-5\n"
          + " line-6\n"
          + " line-7\n"
          + " line-8\n"
          + "@@ -12,4 +12,4 @@ line-11\n"
          + " line-12\n"
          + " line-13\n"
          + " line-14\n"
          + "-line-15\n"
          + "+(CHANGED)line-15";
  private static final String BASE_BRANCH_COMMIT_ID = "base_branch_commit_id";
  private static final String FEATURE_BRANCH_COMMIT_ID = "feature_branch_commit_id";

  private DiffConverter diffConverter;
  private GitRepo gitRepo = mock(GitRepo.class);

  @Before
  public void setUp() {
    diffConverter = new DiffConverter();
    when(gitRepo.getPatch(anyString(), anyString())).thenReturn(TEST_FILE_PATCH);
    when(gitRepo.getTheLatestCommitIdOfBranch(anyString())).thenReturn(BASE_BRANCH_COMMIT_ID);
  }

  @Test
  public void testCommentToFeatureBranchCommit() {
    Protos.Diff diff =
        Protos.Diff.newBuilder()
            .setId(1234)
            .setAuthor(Protos.Author.newBuilder().setEmail("author@test.com").build())
            .setWorkspace("ws1")
            .setCreatedTimestamp(1543989866559L)
            .setModifiedTimestamp(1544008405696L)
            .addCodeThread(
                Protos.Thread.newBuilder()
                    .setRepoId("test-repo")
                    .setCommitId(FEATURE_BRANCH_COMMIT_ID)
                    .setFile(
                        com.google.startupos.common.repo.Protos.File.newBuilder()
                            .setFilename("test_file.txt")
                            .setWorkspace("ws1")
                            .setRepoId("test-repo")
                            .setCommitId(FEATURE_BRANCH_COMMIT_ID)
                            .setFilenameWithRepo("test-repo/test_file.txt")
                            .build())
                    .setLineNumber(5)
                    .addComment(
                        Protos.Comment.newBuilder()
                            .setContent("R 5")
                            .setTimestamp(1544008385129L)
                            .setCreatedBy("reviewer@test.com")
                            .setId("DPwKo")
                            .build())
                    .setId("KblmQG")
                    .build())
            .setModifiedBy("author@test.com")
            .addGithubPr(
                Protos.GithubPr.newBuilder().setOwner("val-fed").setRepo("test-repo").build())
            .build();

    List<GithubPullRequestProtos.PullRequest> actualPullRequest =
        diffConverter.toPullRequests(diff, gitRepo);

    GithubPullRequestProtos.PullRequest expectedPullRequest =
        GithubPullRequestProtos.PullRequest.newBuilder()
            .setState("open")
            .setTitle("D1234")
            .setUser(GithubPullRequestProtos.User.newBuilder().setEmail("author@test.com").build())
            .setCreatedAt("2018-12-05T06:04:26.559Z")
            .setUpdatedAt("2018-12-05T11:13:25.696Z")
            .addReviewComment(
                GithubPullRequestProtos.ReviewComment.newBuilder()
                    .setPath("test_file.txt")
                    .setPosition(5)
                    .setCommitId(FEATURE_BRANCH_COMMIT_ID)
                    .setUser(
                        GithubPullRequestProtos.User.newBuilder()
                            .setEmail("reviewer@test.com")
                            .build())
                    .setBody("R 5")
                    .setCreatedAt("2018-12-05T11:13:05.129Z")
                    .setReviewerThreadId("KblmQG")
                    .build())
            .setBaseBranchName("master")
            .setHeadBranchName("D1234")
            .setRepo("test-repo")
            .setOwner("val-fed")
            .setAssociatedReviewerDiff(1234)
            .build();

    assertEquals(expectedPullRequest.toString(), actualPullRequest.get(0).toString());
  }

  @Test
  public void testCommentToBaseBranchCommit() {
    Protos.Diff diff =
        Protos.Diff.newBuilder()
            .setId(1234)
            .setAuthor(Protos.Author.newBuilder().setEmail("author@test.com").build())
            .setWorkspace("ws1")
            .setCreatedTimestamp(1543989866559L)
            .setModifiedTimestamp(1544008405696L)
            .addCodeThread(
                Protos.Thread.newBuilder()
                    .setRepoId("test-repo")
                    .setCommitId(BASE_BRANCH_COMMIT_ID)
                    .setFile(
                        com.google.startupos.common.repo.Protos.File.newBuilder()
                            .setFilename("test_file.txt")
                            .setWorkspace("ws1")
                            .setRepoId("test-repo")
                            .setCommitId(BASE_BRANCH_COMMIT_ID)
                            .setFilenameWithRepo("test-repo/test_file.txt")
                            .build())
                    .setLineNumber(14)
                    .addComment(
                        Protos.Comment.newBuilder()
                            .setContent("L 14")
                            .setTimestamp(1544008405695L)
                            .setCreatedBy("reviewer@test.com")
                            .setId("s47NL")
                            .build())
                    .setId("ayAF7N")
                    .build())
            .setModifiedBy("author@test.com")
            .addGithubPr(
                Protos.GithubPr.newBuilder().setOwner("val-fed").setRepo("test-repo").build())
            .build();

    List<GithubPullRequestProtos.PullRequest> actualPullRequest =
        diffConverter.toPullRequests(diff, gitRepo);

    GithubPullRequestProtos.PullRequest expectedPullRequest =
        GithubPullRequestProtos.PullRequest.newBuilder()
            .setState("open")
            .setTitle("D1234")
            .setUser(GithubPullRequestProtos.User.newBuilder().setEmail("author@test.com").build())
            .setCreatedAt("2018-12-05T06:04:26.559Z")
            .setUpdatedAt("2018-12-05T11:13:25.696Z")
            .addReviewComment(
                GithubPullRequestProtos.ReviewComment.newBuilder()
                    .setPath("test_file.txt")
                    .setPosition(12)
                    .setCommitId(BASE_BRANCH_COMMIT_ID)
                    .setUser(
                        GithubPullRequestProtos.User.newBuilder()
                            .setEmail("reviewer@test.com")
                            .build())
                    .setBody("L 14")
                    .setCreatedAt("2018-12-05T11:13:25.695Z")
                    .setReviewerThreadId("ayAF7N")
                    .build())
            .setBaseBranchName("master")
            .setHeadBranchName("D1234")
            .setRepo("test-repo")
            .setOwner("val-fed")
            .setAssociatedReviewerDiff(1234)
            .build();

    assertEquals(expectedPullRequest.toString(), actualPullRequest.get(0).toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommentToBaseBranchCommit_NotExistedPositionInGitHub() {
    Protos.Diff diff =
        Protos.Diff.newBuilder()
            .setId(1234)
            .setAuthor(Protos.Author.newBuilder().setEmail("author@test.com").build())
            .setWorkspace("ws1")
            .setCreatedTimestamp(1543989866559L)
            .setModifiedTimestamp(1544008405696L)
            .addCodeThread(
                Protos.Thread.newBuilder()
                    .setRepoId("test-repo")
                    .setCommitId(BASE_BRANCH_COMMIT_ID)
                    .setFile(
                        com.google.startupos.common.repo.Protos.File.newBuilder()
                            .setFilename("test_file.txt")
                            .setWorkspace("ws1")
                            .setRepoId("test-repo")
                            .setCommitId(BASE_BRANCH_COMMIT_ID)
                            .setFilenameWithRepo("test-repo/test_file.txt")
                            .build())
                    .setLineNumber(1)
                    .addComment(
                        Protos.Comment.newBuilder()
                            .setContent("L 1")
                            .setTimestamp(1544008405695L)
                            .setCreatedBy("reviewer@test.com")
                            .setId("s47NL")
                            .build())
                    .setId("ayAF7N")
                    .build())
            .setModifiedBy("author@test.com")
            .addGithubPr(
                Protos.GithubPr.newBuilder().setOwner("val-fed").setRepo("test-repo").build())
            .build();

    diffConverter.toPullRequests(diff, gitRepo);
  }
}

