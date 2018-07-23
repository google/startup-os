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

package com.google.startupos.common.repo.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Repo;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;

public class GitRepoTest {
  private static final String TEST_BRANCH = "test_branch";
  private static final String TEST_FILE = "test_file.txt";
  private static final String TEST_FILE_CONTENTS = "Some test file contents";
  private static final String COMMIT_MESSAGE = "Some commit message";

  private GitRepoFactory gitRepoFactory;
  private Repo repo;
  private GitRepo gitRepo;
  private String repoFolder;
  private FileUtils fileUtils;

  @Before
  public void setup() throws IOException {
    TestComponent component = DaggerGitRepoTest_TestComponent.create();
    gitRepoFactory = component.getFactory();
    fileUtils = component.getFileUtils();
    repoFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    gitRepo = gitRepoFactory.create(repoFolder);
    gitRepo.init();
    repo = gitRepo;
    // We need one commit to make the repo have a master branch.
    repo.commit(repo.getUncommittedFiles(), "Initial commit");
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface TestComponent {
    GitRepoFactory getFactory();

    FileUtils getFileUtils();
  }

  @Test
  public void testThatEmptyRepoHasMasterBranch() {
    assertEquals(ImmutableList.of("master"), repo.listBranches());
  }

  @Test
  public void testAddBranch() {
    repo.switchBranch(TEST_BRANCH);
    assertEquals(ImmutableList.of("master", TEST_BRANCH), repo.listBranches());
  }

  @Test
  public void testRemoveBranch() {
    repo.switchBranch(TEST_BRANCH);
    // Switch to another branch otherwise deleting fails
    repo.switchBranch("master");
    repo.removeBranch(TEST_BRANCH);
    assertEquals(ImmutableList.of("master"), repo.listBranches());
  }

  @Test(expected = RuntimeException.class)
  public void testRemoveNonExistingBranch() {
    repo.removeBranch(TEST_BRANCH);
  }

  @Test
  public void testGetCommitIds() {
    repo.switchBranch(TEST_BRANCH);
    fileUtils.writeStringUnchecked(TEST_FILE_CONTENTS, fileUtils.joinPaths(repoFolder, TEST_FILE));
    repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);
    assertEquals(2, gitRepo.getCommitIds(TEST_BRANCH).size());
  }

  @Test
  public void testGetCommits() {
    repo.switchBranch(TEST_BRANCH);
    fileUtils.writeStringUnchecked(TEST_FILE_CONTENTS, fileUtils.joinPaths(repoFolder, TEST_FILE));
    repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);
    String lastMasterCommitId = gitRepo.getCommitIds(TEST_BRANCH).get(0);
    String commitId = gitRepo.getCommitIds(TEST_BRANCH).get(1);
    assertEquals(
        ImmutableList.of(
            Commit.newBuilder().setId(lastMasterCommitId).build(),
            Commit.newBuilder()
                .setId(commitId)
                .addFile(
                    File.newBuilder()
                        .setAction(File.Action.ADD)
                        .setCommitId(commitId)
                        .setFilename(TEST_FILE)
                        .build())
                .build()),
        repo.getCommits(TEST_BRANCH));
  }

  @Test
  public void testGetMultipleCommits() {
    repo.switchBranch(TEST_BRANCH);
    fileUtils.writeStringUnchecked(TEST_FILE_CONTENTS, fileUtils.joinPaths(repoFolder, TEST_FILE));
    repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);
    fileUtils.writeStringUnchecked("More content", fileUtils.joinPaths(repoFolder, TEST_FILE));
    repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);
    String lastMasterCommit = gitRepo.getCommitIds(TEST_BRANCH).get(0);
    String commitId1 = gitRepo.getCommitIds(TEST_BRANCH).get(1);
    String commitId2 = gitRepo.getCommitIds(TEST_BRANCH).get(2);
    assertEquals(
        ImmutableList.of(
            Commit.newBuilder().setId(lastMasterCommit).build(),
            Commit.newBuilder()
                .setId(commitId1)
                .addFile(
                    File.newBuilder()
                        .setAction(File.Action.ADD)
                        .setCommitId(commitId1)
                        .setFilename(TEST_FILE)
                        .build())
                .build(),
            Commit.newBuilder()
                .setId(commitId2)
                .addFile(
                    File.newBuilder()
                        .setAction(File.Action.MODIFY)
                        .setCommitId(commitId2)
                        .setFilename(TEST_FILE)
                        .build())
                .build()),
        repo.getCommits(TEST_BRANCH));
  }
}

