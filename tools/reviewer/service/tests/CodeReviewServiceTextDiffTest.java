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

import static org.junit.Assert.assertEquals;

import com.google.startupos.common.firestore.FirestoreClientFactory;
import com.google.startupos.tools.reviewer.service.CodeReviewService;
import com.google.startupos.tools.localserver.service.AuthService;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import dagger.Component;
import org.junit.Before;
import org.junit.After;
import com.google.startupos.common.CommonModule;
import com.google.startupos.tools.aa.AaModule;
import com.google.startupos.tools.reviewer.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffRequest;
import com.google.startupos.tools.reviewer.service.Protos.TextDiffResponse;
import java.io.IOException;
import java.nio.file.Files;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.common.flags.Flags;
import javax.inject.Named;
import dagger.Provides;
import java.nio.file.FileSystems;
import com.google.startupos.tools.aa.commands.InitCommand;
import com.google.startupos.tools.aa.commands.WorkspaceCommand;
import com.google.startupos.common.TextDifferencer;
import io.grpc.Server;
import io.grpc.ManagedChannel;
import java.util.concurrent.TimeUnit;
import io.grpc.StatusRuntimeException;
import com.google.startupos.common.repo.Protos.File.Action;

import javax.inject.Singleton;

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
public class CodeReviewServiceTextDiffTest {
  private static final String TEST_FILE = "test_file.txt";
  private static final String TEST_FILE_CONTENTS = "Some test file contents\n";
  private static final String FILE_IN_HEAD = "im_in_head.txt";
  private static final String TEST_WORKSPACE = "ws1";
  private static final String COMMIT_MESSAGE = "Some commit message";

  private GitRepoFactory gitRepoFactory;
  private String aaBaseFolder;
  private String testFileCommitId;
  private String fileInHeadCommitId;
  private GitRepo repo;
  private FileUtils fileUtils;
  private TestComponent component;
  private Server server;
  private ManagedChannel channel;
  private CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;
  private CodeReviewService codeReviewService;

  @Before
  public void setup() throws IOException {
    Flags.parse(
        new String[0], AuthService.class.getPackage(), CodeReviewService.class.getPackage());
    String testFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    String initialRepoFolder = joinPaths(testFolder, "initial_repo");
    aaBaseFolder = joinPaths(testFolder, "base_folder");

    component =
        DaggerCodeReviewServiceTextDiffTest_TestComponent.builder()
            .aaModule(
                new AaModule() {
                  @Provides
                  @Singleton
                  @Override
                  @Named("Base path")
                  public String provideBasePath(FileUtils fileUtils) {
                    return aaBaseFolder;
                  }
                })
            .build();
    gitRepoFactory = component.getGitRepoFactory();
    fileUtils = component.getFileUtils();
    codeReviewService =
        new CodeReviewService(
            component.getAuthService(),
            fileUtils,
            aaBaseFolder,
            gitRepoFactory,
            component.getTextDifferencer(),
            component.getFirestoreClientFactory());

    createInitialRepo(initialRepoFolder);
    initAaBase(initialRepoFolder, aaBaseFolder);
    createAaWorkspace(TEST_WORKSPACE);
    createBlockingStub();
    writeFile(TEST_FILE_CONTENTS);
    testFileCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();
  }

  @After
  public void after() throws InterruptedException {
    server.shutdownNow();
    server.awaitTermination();
    channel.shutdownNow();
    channel.awaitTermination(1, TimeUnit.SECONDS);
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  interface TestComponent {
    FirestoreClientFactory getFirestoreClientFactory();

    AuthService getAuthService();

    GitRepoFactory getGitRepoFactory();

    InitCommand getInitCommand();

    WorkspaceCommand getWorkspaceCommand();

    FileUtils getFileUtils();

    TextDifferencer getTextDifferencer();
  }

  private void createInitialRepo(String initialRepoFolder) {
    fileUtils.mkdirs(initialRepoFolder);
    GitRepo repo = gitRepoFactory.create(initialRepoFolder);
    repo.init();
    repo.setFakeUsersData();
    fileUtils.writeStringUnchecked(
        TEST_FILE_CONTENTS, fileUtils.joinPaths(initialRepoFolder, FILE_IN_HEAD));
    fileInHeadCommitId = repo.commit(repo.getUncommittedFiles(), "Initial commit").getId();
  }

  private void initAaBase(String initialRepoFolder, String aaBaseFolder) {
    InitCommand initCommand = component.getInitCommand();
    InitCommand.basePath.resetValueForTesting();
    InitCommand.startuposRepo.resetValueForTesting();
    String[] args = {
      "--startupos_repo", initialRepoFolder,
      "--base_path", aaBaseFolder,
    };
    initCommand.run(args);
  }

  private void createAaWorkspace(String name) {
    WorkspaceCommand workspaceCommand = component.getWorkspaceCommand();
    String[] args = {"workspace", "-f", name};
    workspaceCommand.run(args);
    String repoPath = fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os");
    repo = gitRepoFactory.create(repoPath);
    repo.setFakeUsersData();
  }

  private void createBlockingStub() throws IOException {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();
    server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(codeReviewService)
            .build()
            .start();
    channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    blockingStub = CodeReviewServiceGrpc.newBlockingStub(channel);
  }

  public String joinPaths(String first, String... more) {
    return FileSystems.getDefault().getPath(first, more).toAbsolutePath().toString();
  }

  private String getWorkspaceFolder(String workspace) {
    return joinPaths(aaBaseFolder, "ws", workspace);
  }

  private TextDiffResponse getResponse(File file) {
    final TextDiffRequest request =
        TextDiffRequest.newBuilder().setLeftFile(file).setRightFile(file).build();
    return blockingStub.getTextDiff(request);
  }

  private TextDiffResponse getExpectedResponse(String contents) {
    return TextDiffResponse.newBuilder()
        .setTextDiff(component.getTextDifferencer().getTextDiff(contents, contents))
        .build();
  }

  private void writeFile(String contents) {
    writeFile(TEST_FILE, contents);
  }

  private void writeFile(String filename, String contents) {
    fileUtils.writeStringUnchecked(
        contents, fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", filename));
  }

  private void deleteFile(String filename) {
    fileUtils.deleteFileOrDirectoryIfExistsUnchecked(
        fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", filename));
  }

  // Committed, workspace exists
  @Test
  public void testTextDiff_committedAndWorkspaceExists() {
    File file =
        File.newBuilder()
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
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
            .setRepoId("startup-os")
            .setWorkspace(TEST_WORKSPACE)
            .setFilename(TEST_FILE)
            .setAction(Action.DELETE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse(""), response);
  }
}

