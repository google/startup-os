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

import com.google.startupos.tools.reviewer.service.CodeReviewService;
import com.google.startupos.tools.localserver.service.AuthService;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;
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
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.common.flags.Flags;
import javax.inject.Named;
import dagger.Module;
import dagger.Provides;
import java.nio.file.FileSystems;
import com.google.startupos.tools.aa.commands.InitCommand;
import com.google.startupos.tools.aa.commands.WorkspaceCommand;
import com.google.startupos.common.TextDifferencer;
import com.google.startupos.common.repo.Protos.Commit;

import javax.inject.Singleton;

/** Unit tests for {@link CodeReviewService}. */
@RunWith(JUnit4.class)
public class CodeReviewServiceTest {
  private static final String TEST_FILE = "test_file.txt";
  private static final String TEST_FILE_CONTENTS = "Some test file contents\n";
  private static final String TEST_WORKSPACE = "ws1";
  private static final String COMMIT_MESSAGE = "Some commit message";

  private GitRepoFactory gitRepoFactory;
  private String aaBaseFolder;
  private FileUtils fileUtils;
  private TextDifferencer textDifferencer;
  TestComponent component;
  CodeReviewServiceGrpc.CodeReviewServiceBlockingStub blockingStub;

  @Before
  public void setup() throws IOException {
    Flags.parse(
        new String[0], AuthService.class.getPackage(), CodeReviewService.class.getPackage());
    String testFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    String initialRepoFolder = joinPaths(testFolder, "initial_repo");
    aaBaseFolder = joinPaths(testFolder, "base_folder");

    component =
        DaggerCodeReviewServiceTest_TestComponent.builder()
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
    gitRepoFactory = component.getFactory();
    fileUtils = component.getFileUtils();

    createInitialRepo(initialRepoFolder);
    initAaBase(initialRepoFolder, aaBaseFolder);
    createAaWorkspace(TEST_WORKSPACE);
    createBlockingStub();
  }

  private void createInitialRepo(String initialRepoFolder) {
    fileUtils.mkdirs(initialRepoFolder);
    GitRepo repo = gitRepoFactory.create(initialRepoFolder);
    repo.init();
    // We need one commit to make the repo have a master branch.
    repo.commit(repo.getUncommittedFiles(), "Initial commit");
  }

  private void initAaBase(String initialRepoFolder, String aaBaseFolder) {
    InitCommand initCommand = component.getInitCommand();
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
  }

  private void createBlockingStub() throws IOException {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();
    InProcessServerBuilder.forName(serverName)
        .directExecutor()
        .addService(component.getCodeReviewService())
        .build()
        .start();
    blockingStub =
        CodeReviewServiceGrpc.newBlockingStub(
            InProcessChannelBuilder.forName(serverName).directExecutor().build());
  }

  public String joinPaths(String first, String... more) {
    return FileSystems.getDefault().getPath(first, more).toAbsolutePath().toString();
  }

  @Singleton
  @Component(modules = {CommonModule.class, AaModule.class})
  interface TestComponent {
    CodeReviewService getCodeReviewService();

    GitRepoFactory getFactory();

    InitCommand getInitCommand();

    WorkspaceCommand getWorkspaceCommand();

    FileUtils getFileUtils();

    TextDifferencer getTextDifferencer();
  }

  private String getWorkspaceFolder(String workspace) {
    return joinPaths(aaBaseFolder, "ws", workspace);
  }

  TextDiffResponse getResponse(File file) {
    final TextDiffRequest request =
        TextDiffRequest.newBuilder().setLeftFile(file).setRightFile(file).build();
    return blockingStub.getTextDiff(request);
  }

  TextDiffResponse getExpectedResponse(String contents) {
    return TextDiffResponse.newBuilder()
        .addAllChanges(component.getTextDifferencer().getAllTextChanges(contents, contents))
        .setLeftFileContents(contents)
        .setRightFileContents(contents)
        .build();
  }

  @Test
  public void testTextDiff_untrackedlocallyModifiedFile() throws Exception {
    fileUtils.writeStringUnchecked(
        TEST_FILE_CONTENTS,
        fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os", TEST_FILE));
    File file =
        File.newBuilder()
            .setRepoId("startup-os")
            .setWorkspace(TEST_WORKSPACE)
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    // TODO: Once we fix the stripping of the last newline in FileUtils.readFile(), remove tempFix.
    String tempFix = TEST_FILE_CONTENTS.substring(0, 23);
    assertEquals(getExpectedResponse(tempFix), response);
  }

  @Test
  public void testTextDiff_committedFile() throws Exception {
    String repoPath = fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os");
    fileUtils.writeStringUnchecked(TEST_FILE_CONTENTS, fileUtils.joinPaths(repoPath, TEST_FILE));
    GitRepo repo = gitRepoFactory.create(repoPath);
    Commit commit = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);

    File file =
        File.newBuilder()
            .setRepoId("startup-os")
            .setWorkspace(TEST_WORKSPACE)
            .setCommitId(commit.getId())
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    // TODO: Once we fix the stripping of the last newline in FileUtils.readFile(), remove tempFix.
    String tempFix = TEST_FILE_CONTENTS.substring(0, 23);
    assertEquals(getExpectedResponse(tempFix), response);
  }

  @Test
  public void testTextDiff_committedModifiedFile() throws Exception {
    String repoPath = fileUtils.joinPaths(getWorkspaceFolder(TEST_WORKSPACE), "startup-os");
    fileUtils.writeStringUnchecked(TEST_FILE_CONTENTS, fileUtils.joinPaths(repoPath, TEST_FILE));
    GitRepo repo = gitRepoFactory.create(repoPath);
    Commit commit = repo.commit(repo.getUncommittedFiles(), COMMIT_MESSAGE);
    fileUtils.writeStringUnchecked("Some changes", fileUtils.joinPaths(repoPath, TEST_FILE));

    File file =
        File.newBuilder()
            .setRepoId("startup-os")
            .setWorkspace(TEST_WORKSPACE)
            .setFilename(TEST_FILE)
            .build();

    TextDiffResponse response = getResponse(file);
    assertEquals(getExpectedResponse("Some changes"), response);
  }
}

