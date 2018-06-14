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
import com.google.startupos.common.repo.Repo;
import dagger.Component;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;

public class GitRepoTest {
  GitRepoFactory gitRepoFactory;
  Repo repo;
  String repoFolder;
  FileUtils fileUtils;

  @Before
  public void setup() throws IOException {
    TestComponent component = DaggerGitRepoTest_TestComponent.create();
    gitRepoFactory = component.getFactory();
    fileUtils = component.getFileUtils();
    repoFolder = Files.createTempDirectory("temp").toAbsolutePath().toString();
    GitRepo gitRepo = gitRepoFactory.create(repoFolder);
    gitRepo.init();
    repo = gitRepo;
    // We need one commit to make the repo have a master branch.
    fileUtils.writeStringUnchecked("some contents", repoFolder + "/some_file.txt");
    repo.commit(repo.getUncommittedFiles(), "Some commit message");
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  public interface TestComponent {
    GitRepoFactory getFactory();
    FileUtils getFileUtils();
  }

  @Test
  public void testThatEmptyRepoHasMasterBranch() {
    assertEquals(ImmutableList.of("master"), repo.listBranches());
  }

  @Test
  public void testAddBranch() {
    repo.switchBranch("testBranch");
    assertEquals(ImmutableList.of("master", "testBranch"), repo.listBranches());
  }

  @Test
  public void testRemoveBranch() {
    repo.switchBranch("testBranch");
    // Switch to another branch otherwise deleting fails
    repo.switchBranch("master");
    repo.removeBranch("testBranch");
    assertEquals(ImmutableList.of("master"), repo.listBranches());
  }

  @Test(expected = RuntimeException.class)
  public void testRemoveNonExistingBranch() {
    repo.removeBranch("testBranch");
  }
}
