package com.google.startupos.common.repo;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.tools.reviewer.service.Protos.File;


/** An interface for a code repository that uses Trunk-based development.
 * In particular, this means all branches start from master and merge to master.
 * Underlying repo could be Git, Mercurial, or a MemoryRepo for testing.
 * 
 * Where possible, if an action fails or takes a long time (and can be cancelled by the user), the
 * implementation should do them in a way that has no side-effect (e.g doing operations on a temp
 * branch/folder and copying when done).
 */
public interface Repo {
  /** Switches to branch. Creates branch if needed. */
  void switchBranch(String branch);
  /** Gets commits on branch since it diverged from master. */
  ImmutableList<Commit> getCommits(String branch);
  /** Gets all uncommited files. This includes new, modified and deleted files. */
  ImmutableList<File> getUncommittedFiles();
  /** Commits files to current branch and returns commit */
  Commit commit(ImmutableList<File> files, String message);
  /** Pushes all branches to remote repo */
  void pushAll();
  /** Pulls all branches from remote repo */
  void pullAll();
  /** Merges branch to master. Returns true on success. */
  boolean merge(String branch);
  /** Is branch merged to master */
  boolean isMerged(String branch);
  String getFileContents(File file);
}
