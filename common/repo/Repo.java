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

package com.google.startupos.common.repo;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.File;
import java.io.IOException;

/**
 * An interface for a code repository that uses Trunk-based development. In particular, this means
 * all branches start from master and merge to master. Underlying repo could be Git, Mercurial, or a
 * memory repo for testing.
 *
 * <p>Where possible, if an action fails or takes a long time (and can be cancelled by the user),
 * the implementation should do them in a way that has no side-effect (e.g doing operations on a
 * temp branch/folder and copying when done).
 */
public interface Repo {
  /** Switches to branch. Creates branch if needed. */
  void switchBranch(String branch);

  /** Tags the commit at head. */
  void tagHead(String name);

  /**
   * Gets commits on branch since it diverged from master, including the last master commit, at
   * position 0.
   */
  ImmutableList<Commit> getCommits(String branch);

  /** Gets all uncommited files. This includes new, modified and deleted files. */
  ImmutableList<File> getUncommittedFiles();

  /** Does commit exist. */
  boolean commitExists(String commitId);

  /** Gets files in commit. */
  ImmutableList<File> getFilesInCommit(String commitId);

  /** Commits files to current branch and returns commit. */
  Commit commit(ImmutableList<File> files, String message);

  /** Pushes branch to remote repo. */
  boolean push(String branch);

  /** Pulls all branches from remote repo. */
  void pull();

  /** Merges branch to master, keeping conflicting changes in tree. * Returns true on success. */
  boolean merge(String branch);

  /** Is branch merged to master. */
  boolean isMerged(String branch);

  /**
   * Reset current branch. All changes introduced after it would be marked as unstaged but saved in
   * working tree
   */
  void reset(String ref);

  /**
   * Resets the index and working tree. Any changes to tracked files in the working tree since
   * `commitId` are discarded.
   */
  void resetHard(String commitId);

  /** Remove branch. */
  void removeBranch(String branch);

  /** List branches. */
  ImmutableList<String> listBranches();

  boolean branchExists(String name);

  boolean fileExists(String commitId, String path);

  boolean fileExists(File file);

  String getTextDiff(File file1, File file2) throws IOException;

  String getFileContents(String commitId, String path);

  /** Get current branch name. */
  String currentBranch();

  /** Retrieves the URLs for a remote (e.g. https://github.com/google/startup-os.git). */
  String getRemoteUrl();

  /** Checks if there are commits added since master, or any uncommitted files. */
  boolean hasChanges(String branch);

  /**
   * Gets patch(diff) between file in `firstReferenceCommitOrBranch` and the file in the
   * `secondReferenceCommitOrBranch`. It's possible to use branch name or commit ID. The response
   * can have one and more diff hunks
   */
  String getPatch(
      String firstReferenceCommitOrBranch, String secondReferenceCommitOrBranch, String filename);

  String getMostRecentCommitOfBranch(String branch);

  String getMostRecentCommitOfFile(String filename);
}

