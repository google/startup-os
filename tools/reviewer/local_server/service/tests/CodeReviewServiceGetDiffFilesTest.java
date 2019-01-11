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

package com.google.startupos.tools.reviewer.localserver.service.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.TextDifferencer;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.common.repo.Protos.BranchInfo;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.File;
import com.google.startupos.tools.localserver.service.AuthService;
import com.google.startupos.tools.reviewer.aa.AaModule;
import com.google.startupos.tools.reviewer.aa.commands.InitCommand;
import com.google.startupos.tools.reviewer.aa.commands.WorkspaceCommand;
import com.google.startupos.tools.reviewer.localserver.service.CodeReviewService;
import com.google.startupos.tools.reviewer.localserver.service.CodeReviewServiceGrpc;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffFilesRequest;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffFilesResponse;
import com.google.startupos.tools.reviewer.localserver.service.Protos.DiffNumberResponse;
import dagger.Component;
import dagger.Provides;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CodeReviewServiceGetDiffFilesTest {
  private static final String TEST_FILE = "test_file.txt";
  private static final String TEST_FILE_CONTENTS = "Some test file contents\n";
  private static final String FILE_IN_HEAD = "im_in_head.txt";
  private static final String TEST_WORKSPACE = "ws1";
  private static final String COMMIT_MESSAGE = "Some commit message";
  private static final String REPO_ID = "startup-os";
  private static final int DIFF_ID = 2;

  private GitRepoFactory gitRepoFactory;
  private String aaBaseFolder;
  private String testFileCommitId;
  private String fileInHeadCommitId;
  private GitRepo repo;
  private FileUtils fileUtils;
  private CodeReviewServiceGetDiffFilesTest.TestComponent component;
  private Server server;
  private ManagedChannel channel;
  private CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;
  private CodeReviewService codeReviewService;
  // set by createAaWorkspace
  private String repoPath;

  private FirestoreProtoClient firestoreClient = mock(FirestoreProtoClient.class);

  @Before
  public void setup() throws IOException {
    Flags.parse(
        new String[0], AuthService.class.getPackage(), CodeReviewService.class.getPackage());
    component =
        DaggerCodeReviewServiceGetDiffFilesTest_TestComponent.builder()
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
    String testFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    String initialRepoFolder = fileUtils.joinToAbsolutePath(testFolder, "initial_repo");
    aaBaseFolder = fileUtils.joinToAbsolutePath(testFolder, "base_folder");

    codeReviewService =
        new CodeReviewService(
            component.getAuthService(),
            fileUtils,
            aaBaseFolder,
            gitRepoFactory,
            component.getTextDifferencer());

    createInitialRepo(initialRepoFolder);
    initAaBase(initialRepoFolder, aaBaseFolder);
    createAaWorkspace(TEST_WORKSPACE);
    createBlockingStub();
    writeFile(TEST_FILE_CONTENTS);
    testFileCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();
    mockFirestoreClientMethods();
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
    repo.setUserDataForTesting();
    fileUtils.writeStringUnchecked(
        TEST_FILE_CONTENTS, fileUtils.joinPaths(initialRepoFolder, FILE_IN_HEAD));
    fileInHeadCommitId = repo.commit(repo.getUncommittedFiles(), "Initial commit").getId();
  }

  private void initAaBase(String initialRepoFolder, String aaBaseFolder) {
    InitCommand initCommand = component.getInitCommand();
    String[] args = {
      "init", "--base_path", aaBaseFolder, "--startupos_repo", initialRepoFolder,
    };
    initCommand.run(args);
  }

  private void createAaWorkspace(String name) {
    WorkspaceCommand workspaceCommand = component.getWorkspaceCommand();
    String[] args = {"workspace", "-f", name};
    workspaceCommand.run(args);
    repoPath = fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os");
    repo = gitRepoFactory.create(repoPath);
    repo.setUserDataForTesting();
    repo.switchBranch("D" + DIFF_ID);
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

  private void writeFile(String contents) {
    writeFile(TEST_FILE, contents);
  }

  private void writeFile(String filename, String contents) {
    fileUtils.writeStringUnchecked(
        contents, fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", filename));
  }

  private void mockFirestoreClientMethods() {
    when(firestoreClient.getProtoDocument(
            "reviewer/data/diff/" + DIFF_ID, DiffFilesResponse.newBuilder()))
        .thenReturn(
            DiffFilesResponse.newBuilder()
                .addAllBranchInfo(
                    Collections.singleton(
                        BranchInfo.newBuilder()
                            .setDiffId(DIFF_ID)
                            .setRepoId(REPO_ID)
                            .addCommit(Commit.newBuilder().setId(fileInHeadCommitId))
                            .build()))
                .build());

    when(firestoreClient.getProtoDocument(
            "/reviewer/data/last_diff_id", DiffNumberResponse.newBuilder()))
        .thenReturn(DiffNumberResponse.newBuilder().setLastDiffId(1).build());
  }

  private String getWorkspaceFolder(String workspace) {
    return fileUtils.joinToAbsolutePath(aaBaseFolder, "ws", workspace);
  }

  private DiffFilesResponse getResponse() {
    DiffFilesRequest request =
        DiffFilesRequest.newBuilder().setWorkspace(TEST_WORKSPACE).setDiffId(DIFF_ID).build();

    return blockingStub.getDiffFiles(request);
  }

  private DiffFilesResponse getExpectedResponseAddingCommit(Commit commit) {
    BranchInfo.Builder branchInfoBuilder = getExpectedBranchInfo().toBuilder();
    if (commit != null) {
      branchInfoBuilder.addCommit(commit).build();
    }
    return DiffFilesResponse.newBuilder().addBranchInfo(branchInfoBuilder).build();
  }

  private DiffFilesResponse getExpectedResponseAddingUncommittedFile(File file) {
    BranchInfo.Builder branchInfoBuilder = getExpectedBranchInfo().toBuilder();
    if (file != null) {
      branchInfoBuilder.addUncommittedFile(file).build();
    }
    return DiffFilesResponse.newBuilder().addBranchInfo(branchInfoBuilder).build();
  }

  private BranchInfo getExpectedBranchInfo() {
    return BranchInfo.newBuilder()
        .setDiffId(DIFF_ID)
        .setRepoId(REPO_ID)
        .addCommit(Commit.newBuilder().setId(fileInHeadCommitId).build())
        .addCommit(
            Commit.newBuilder()
                .setId(testFileCommitId)
                .addFile(
                    File.newBuilder()
                        .setFilename("test_file.txt")
                        .setWorkspace("ws1")
                        .setRepoId(REPO_ID)
                        .setCommitId(testFileCommitId)
                        .setFilenameWithRepo("startup-os/test_file.txt")
                        .build())
                .build())
        .build();
  }

  @Test
  public void testGetDiffFiles_withoutChangesInWorkspace() {
    assertEquals(getExpectedResponseAddingCommit(null), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenFileIsModified() {
    writeFile("new_file_content");
    final String lastCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();

    Commit lastCommit =
        Commit.newBuilder()
            .setId(lastCommitId)
            .addFile(
                File.newBuilder()
                    .setFilename("test_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setAction(File.Action.MODIFY)
                    .setFilenameWithRepo("startup-os/test_file.txt")
                    .build())
            .build();

    assertEquals(getExpectedResponseAddingCommit(lastCommit), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenNewFileIsAdded() {
    writeFile("new_file.txt", "file content");
    final String lastCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();

    Commit lastCommit =
        Commit.newBuilder()
            .setId(lastCommitId)
            .addFile(
                File.newBuilder()
                    .setFilename("new_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setFilenameWithRepo("startup-os/new_file.txt")
                    .build())
            .build();

    assertEquals(getExpectedResponseAddingCommit(lastCommit), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenFileIsRenamed() throws IOException {
    // rename the file
    Files.move(
        Paths.get(fileUtils.joinToAbsolutePath(repoPath, TEST_FILE)),
        Paths.get(fileUtils.joinToAbsolutePath(repoPath, "new_filename.txt")),
        StandardCopyOption.REPLACE_EXISTING);

    repo.addFile("new_filename.txt");
    repo.addFile(TEST_FILE);

    File uncommittedFile =
        File.newBuilder()
            .setFilename("new_filename.txt")
            .setWorkspace("ws1")
            .setRepoId("startup-os")
            .setAction(File.Action.RENAME)
            .setOriginalFilename("test_file.txt")
            .setFilenameWithRepo("startup-os/new_filename.txt")
            .build();

    assertEquals(getExpectedResponseAddingUncommittedFile(uncommittedFile), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenNewFileIsDeleted() throws IOException {
    fileUtils.deleteFileOrDirectoryIfExists(
        fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", TEST_FILE));
    final String lastCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();

    Commit lastCommit =
        Commit.newBuilder()
            .setId(lastCommitId)
            .addFile(
                File.newBuilder()
                    .setFilename("test_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setAction(File.Action.DELETE)
                    .setFilenameWithRepo("startup-os/test_file.txt")
                    .build())
            .build();

    assertEquals(getExpectedResponseAddingCommit(lastCommit), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenFileIsModifiedAndNewFileIsAdded() {
    writeFile("new_file_content");
    writeFile("new_file.txt", "file content");
    final String lastCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();

    Commit lastCommit =
        Commit.newBuilder()
            .setId(lastCommitId)
            .addFile(
                File.newBuilder()
                    .setFilename("new_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setFilenameWithRepo("startup-os/new_file.txt")
                    .build())
            .addFile(
                File.newBuilder()
                    .setFilename("test_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setAction(File.Action.MODIFY)
                    .setFilenameWithRepo("startup-os/test_file.txt")
                    .build())
            .build();

    assertEquals(getExpectedResponseAddingCommit(lastCommit), getResponse());
  }

  @Test
  public void testGetDiffFiles_whenFileIsDeletedAndNewFileIsAdded() throws IOException {
    writeFile("new_file.txt", "file content");
    fileUtils.deleteFileOrDirectoryIfExists(
        fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", TEST_FILE));
    final String lastCommitId = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE).getId();

    Commit lastCommit =
        Commit.newBuilder()
            .setId(lastCommitId)
            .addFile(
                File.newBuilder()
                    .setFilename("new_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setFilenameWithRepo("startup-os/new_file.txt")
                    .build())
            .addFile(
                File.newBuilder()
                    .setFilename("test_file.txt")
                    .setWorkspace("ws1")
                    .setRepoId(REPO_ID)
                    .setCommitId(lastCommitId)
                    .setAction(File.Action.DELETE)
                    .setFilenameWithRepo("startup-os/test_file.txt")
                    .build())
            .build();

    assertEquals(getExpectedResponseAddingCommit(lastCommit), getResponse());
  }
}

