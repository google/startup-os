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

import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.common.repo.Protos.File.Action;
import com.google.startupos.tools.reviewer.service.Protos;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffResponse;
import io.grpc.StatusRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for {@link CodeReviewService}.
 *
 * There are 5 file modes: ADD, DELETE, RENAME, MODIFY and COPY
 * We separate the tests into these modes.
 *
 * Any mode:
 * - Committed, workspace exists - Committed, workspace doesn't exist
 * - Committed, workspace doesn't exist (pushed)
 * - Head
 *
 * ADD:
 * - Locally modified, workspace exists, new file
 * - Locally modified, workspace doesn't exist, new file
 *
 * MODIFY, RENAME, COPY:
 * - Locally modified, workspace exists, previously committed
 *
 * DELETE:
 * - Any deleted file should return ""
 */
@RunWith(JUnit4.class)
public class CodeReviewServiceTextDiffTest extends CodeReviewServiceTest {

  private Protos.TextDiffResponse getExpectedResponse(String contents) {
    return Protos.TextDiffResponse.newBuilder()
        .setTextDiff(component.getTextDifferencer().getTextDiff(contents, contents))
        .build();
  }

  private Protos.TextDiffResponse getResponse(com.google.startupos.common.repo.Protos.File file) {
    final Protos.TextDiffRequest request =
        Protos.TextDiffRequest.newBuilder().setLeftFile(file).setRightFile(file).build();
    return blockingStub.getTextDiff(request);
  }

  // Committed, workspace exists
  @Test
  public void testTextDiff_committedAndWorkspaceExists() {
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setCommitId(testFileCommitId)
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // Committed, workspace doesn't exist
  @Test
  public void testTextDiff_committedAndWorkspaceNotExists() {
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace("non-existing-workspace")
            .setCommitId(testFileCommitId)
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(""), response);
  }

  // Committed, workspace doesn't exist (pushed)
  @Test
  public void testTextDiff_committedAndWorkspaceNotExists_pushed() {
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace("non-existing-workspace")
            .setCommitId(fileInHeadCommitId)
            .setFilename(FILE_IN_HEAD)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // File in head
  @Test
  public void testTextDiff_fileInHead() {
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setCommitId(fileInHeadCommitId)
            .setFilename(FILE_IN_HEAD)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // ADD, locally modified, workspace exists, new file
  @Test
  public void testTextDiff_locallyModifiedWorkspaceExistsNewFile() {
    writeFile("somefile.txt", TEST_FILE_CONTENTS);
    writeFile(TEST_FILE_CONTENTS);
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setFilename("somefile.txt")
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // ADD, locally modified, workspace doesn't exist, new file
  @Test(expected = StatusRuntimeException.class)
  public void testTextDiff_locallyModifiedWorkspaceNotExistsNewFile() {
    writeFile("somefile.txt", TEST_FILE_CONTENTS);
    writeFile(TEST_FILE_CONTENTS);
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace("non-existing-workspace")
            .setFilename("somefile.txt")
            .build();

    TextDiffResponse response = getResponse(file);
  }

  // MODIFY, locally modified, workspace exists, previously committed
  @Test
  public void testTextDiff_locallyModifiedWorkspaceExistsPreviouslyCommitted() {
    writeFile("Some changes");

    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse("Some changes"), response);
  }

  // RENAME, locally modified, workspace exists, previously committed
  @Test
  public void renamedWorkspaceExistsPreviouslyCommitted() {
    writeFile("renamed.txt", TEST_FILE_CONTENTS);
    deleteFile(TEST_FILE);

    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setFilename("renamed.txt")
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // COPY, locally modified, workspace exists, previously committed
  @Test
  public void copiedWorkspaceExistsPreviouslyCommitted() {
    writeFile("copied.txt", TEST_FILE_CONTENTS);

    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setFilename("copied.txt")
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(TEST_FILE_CONTENTS), response);
  }

  // DELETE, any file
  @Test
  public void testTextDiff_deletedFile() {
    File file =
        File.newBuilder()
            .setRepoId(REPO_ID)
            .setWorkspace(TEST_WORKSPACE)
            .setFilename(TEST_FILE)
            .setAction(Action.DELETE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(""), response);
  }
}

