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

package com.google.startupos.tools.reviewer.service.tests;

import com.google.common.collect.ImmutableList;
import com.google.startupos.tools.aa.AaModule;
import com.google.startupos.tools.aa.commands.DiffCommand;
import com.google.startupos.tools.reviewer.service.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class CodeReviewServiceDiffFilesTest extends CodeReviewServiceTest {

  private Protos.DiffFilesResponse getResponse(String workspace, long diffNumber) {
    Protos.DiffFilesRequest request =
        Protos.DiffFilesRequest.newBuilder().setWorkspace(workspace).setDiffId(diffNumber).build();
    return blockingStub.getDiffFiles(request);
  }

  private Integer getDiffNumber() {
    return AaModule.diffNumber(fileUtils, getWorkspaceFolder(TEST_WORKSPACE), gitRepoFactory);
  }

  private DiffCommand getDiffCommand() {
    return new DiffCommand(
        fileUtils,
        gitRepoFactory,
        TEST_WORKSPACE,
        getWorkspaceFolder(TEST_WORKSPACE),
        getDiffNumber());
  }

  private void runDiffCommand() {
    String[] args = {"diff"};
    getDiffCommand().run(args);
  }

  private Protos.DiffFilesResponse getExpectedResponse(String commitId) {
    return Protos.DiffFilesResponse.newBuilder()
        .addAllBranchInfo(
            Collections.singleton(
                com.google.startupos.common.repo.Protos.BranchInfo.newBuilder()
                    .setDiffId(getDiffNumber())
                    .setRepoId(REPO_ID)
                    .addCommit(
                        com.google.startupos.common.repo.Protos.Commit.newBuilder().setId(commitId))
                    .build()))
        .build();
  }

  private Protos.DiffFilesResponse getExpectedResponse(String commitId, String fileName) {
    return Protos.DiffFilesResponse.newBuilder()
        .addAllBranchInfo(
            Collections.singleton(
                com.google.startupos.common.repo.Protos.BranchInfo.newBuilder()
                    .setDiffId(getDiffNumber())
                    .setRepoId(REPO_ID)
                    .addCommit(
                        com.google.startupos.common.repo.Protos.Commit.newBuilder().setId(commitId))
                    .addUncommittedFile(
                        com.google.startupos.common.repo.Protos.File.newBuilder()
                            .setFilename(fileName)
                            .setWorkspace(TEST_WORKSPACE)
                            .setRepoId(REPO_ID))
                    .build()))
        .build();
  }

  private Protos.DiffFilesResponse getExpectedResponse(
      String commitId, ImmutableList<com.google.startupos.common.repo.Protos.File> files) {
    return Protos.DiffFilesResponse.newBuilder()
        .addAllBranchInfo(
            Collections.singleton(
                com.google.startupos.common.repo.Protos.BranchInfo.newBuilder()
                    .setDiffId(getDiffNumber())
                    .setRepoId(REPO_ID)
                    .addCommit(
                        com.google.startupos.common.repo.Protos.Commit.newBuilder().setId(commitId))
                    .addAllUncommittedFile(files)
                    .build()))
        .build();
  }

  @Test
  public void testDiffFiles_withoutChanges() {
    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());
    Protos.DiffFilesResponse expectedResponse = getExpectedResponse(testFileCommitId);

    assertEquals(expectedResponse, response);
  }

  @Test
  public void testDiffFiles_addUncommittedFile() {
    writeFile("new_file.txt", "file content");

    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());
    Protos.DiffFilesResponse expectedResponse =
        getExpectedResponse(testFileCommitId, "new_file.txt");

    assertEquals(expectedResponse, response);
  }

  @Test
  public void testDiffFiles_addTwoUncommittedFiles() {
    writeFile("first_file.txt", "file content");
    com.google.startupos.common.repo.Protos.File firstFile =
        com.google.startupos.common.repo.Protos.File.newBuilder()
            .setFilename("first_file.txt")
            .setWorkspace(TEST_WORKSPACE)
            .setRepoId(REPO_ID)
            .build();
    writeFile("second_file.txt", "file content");
    com.google.startupos.common.repo.Protos.File secondFile =
        com.google.startupos.common.repo.Protos.File.newBuilder()
            .setFilename("second_file.txt")
            .setWorkspace(TEST_WORKSPACE)
            .setRepoId(REPO_ID)
            .build();
    ImmutableList<com.google.startupos.common.repo.Protos.File> uncommittedFiles =
        ImmutableList.of(firstFile, secondFile);

    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());

    Protos.DiffFilesResponse expectedResponse =
        getExpectedResponse(testFileCommitId, uncommittedFiles);

    assertEquals(expectedResponse, response);
  }

  @Test
  public void testDiffFiles_addCommittedFile() {
    writeFile("new_file.txt", "new content");
    String commitId = repo.commit(repo.getUncommittedFiles(), "commit message").getId();

    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());
    Protos.DiffFilesResponse expectedResponse = getExpectedResponse(commitId);

    assertEquals(expectedResponse, response);
  }

  @Test
  public void testDiffFiles_modifyFile() {
    writeFile(TEST_FILE, "new content");
    String commitId = repo.commit(repo.getUncommittedFiles(), "commit message").getId();
    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());
    Protos.DiffFilesResponse expectedResponse = getExpectedResponse(commitId);

    assertEquals(expectedResponse, response);
  }

  @Test
  public void testDiffFiles_deleteFile() {
    String testFilePath = getWorkspaceFolder(fileUtils.joinPaths(TEST_WORKSPACE, TEST_FILE));
    fileUtils.deleteFileOrDirectoryIfExistsUnchecked(testFilePath);
    String commitId = repo.commit(repo.getUncommittedFiles(), "commit message").getId();
    runDiffCommand();

    Protos.DiffFilesResponse response = getResponse(TEST_WORKSPACE, getDiffNumber());
    Protos.DiffFilesResponse expectedResponse = getExpectedResponse(commitId);

    assertFalse(fileUtils.fileExists(testFilePath));
    assertEquals(expectedResponse, response);
  }
}

