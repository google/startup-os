package com.google.startupos.common.repo.tests;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
