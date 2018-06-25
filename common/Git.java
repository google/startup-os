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

package com.google.startupos.common;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.io.IOException;

/** Git utils */
public class Git {
  private final org.eclipse.jgit.api.Git git;

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
