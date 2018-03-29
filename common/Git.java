package com.appstory.common;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.io.IOException;

/** Git utils */
public class Git {
  private org.eclipse.jgit.api.Git git;

  public Git(String repoPath) {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try {
      Repository repository =
          builder
              .setGitDir(Paths.get(repoPath, ".git").toFile())
              .readEnvironment() // scan environment GIT_* variables
              .findGitDir() // scan up the file system tree
              .build();
      git = new org.eclipse.jgit.api.Git(repository);
    } catch (IOException e) {
      throw new RuntimeException("Error initializing Git", e);
    }
  }

  /**
   * Commit to a git repo
   *
   * <p>File patten is a file or folder. For folder - all files are commited.
   */
  public void commit(String filePattern, String commitMessage) {
    commit(ImmutableList.of(filePattern), commitMessage);
  }

  /**
   * Commit files to a git repo
   *
   * <p>File pattens are files or folders. For folders - all files are commited.
   */
  public void commit(ImmutableList<String> filePatterns, String commitMessage) {
    try {
      for (String filePattern : filePatterns) {
        git.add().addFilepattern(filePattern).call();
      }
      git.commit().setMessage(commitMessage).call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void pull() {
    try {
      git.pull().call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void push() {
    try {
      git.push().call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }
}

