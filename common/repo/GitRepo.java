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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.tools.reviewer.service.Protos.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Paths;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

// TODO: Implement methods
@AutoFactory
public class GitRepo implements Repo {
  private Git jGit;
  private Repository jGitRepo;

  GitRepo(@Provided FileSystem fileSystem, String repoPath) {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try {
      jGitRepo =
          builder
              .setGitDir(Paths.get(repoPath, ".git").toFile())
              .readEnvironment() // Scan environment GIT_* variables
              .findGitDir() // Scan up the file system tree
              .build();
      jGit = new Git(jGitRepo);
    } catch (IOException e) {
      throw new RuntimeException("Error initializing Git", e);
    }
  }

  public void switchBranch(String branch) {
    boolean createBranch = true;
    try {
      Ref ref = jGitRepo.exactRef("refs/heads/" + branch);
      if (ref != null) {
        createBranch = false;
      }
      jGit.checkout().setCreateBranch(createBranch).setName(branch).call();
    } catch (IOException | GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public ImmutableList<Commit> getCommits(String branch) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public ImmutableList<File> getUncommittedFiles() {
    ImmutableList.Builder<File> files = ImmutableList.builder();
    try {
      Status status = jGit.status().call();
      status
          .getAdded()
          .forEach(
              added ->
                  files.add(
                      File.newBuilder().setAction(File.Action.ADD).setFilename(added).build()));
      status
          .getModified()
          .forEach(
              changed ->
                  files.add(
                      File.newBuilder()
                          .setAction(File.Action.MODIFY)
                          .setFilename(changed)
                          .build()));
      status
          .getRemoved()
          .forEach(
              removed ->
                  files.add(
                      File.newBuilder()
                          .setAction(File.Action.DELETE)
                          .setFilename(removed)
                          .build()));
      status
          .getUntracked()
          .forEach(
              untracked ->
                  files.add(
                      File.newBuilder()
                          .setAction(File.Action.UNRECOGNIZED)
                          .setFilename(untracked)
                          .build()));
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return files.build();
  }

  public Commit commit(ImmutableList<File> files, String message) {
    try {
      for (File file : files) {
        jGit.add().addFilepattern(file.getFilename()).call();
      }
      RevCommit revCommit = jGit.commit().setMessage(message).call();
      return Commit.newBuilder().setId(revCommit.getId().toString()).addAllFile(files).build();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void pushAll() {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void pull() {
    try {
      jGit.pull().call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean merge(String branch) {
    try {
      Ref current = jGitRepo.exactRef(jGitRepo.getFullBranch());
      boolean successfulMerge = true;
      Ref ref = jGitRepo.exactRef("refs/heads/" + branch);
      AddCommand addCommand = jGit.add();
      jGit.merge().include(ref).call();
      for (String conflictingFile: jGit.status().call().getConflicting()) {
        successfulMerge = false;
        addCommand = addCommand.addFilepattern(conflictingFile);
      }
      if (!successfulMerge) {
        addCommand.call();
        jGit.commit().call();
      }
      jGit.reset().setRef(current.getObjectId().toObjectId().name()).call();
      return successfulMerge;
    } catch (GitAPIException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isMerged(String branch) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void reset(String ref) {
    try {
      // MIXED type means that HEAD pointer would be
      // reset to `ref` and all changes introduced after it
      // would be marked as unstaged but saved in working tree
      jGit.reset().setMode(ResetType.MIXED).setRef(ref).call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeBranch(String branch) {
    try {
      jGit.branchDelete().setBranchNames(branch).setForce(true).call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public String getFileContents(String commitId, String path) {
    ObjectReader objectReader = null;
    try {
      final ObjectId objectId = jGitRepo.resolve(commitId);
      objectReader = jGitRepo.newObjectReader();
      RevWalk revWalk = new RevWalk(objectReader);
      RevCommit revCommit = revWalk.parseCommit(objectId);
      RevTree revTree = revCommit.getTree();
      TreeWalk treeWalk = TreeWalk.forPath(objectReader, path, revTree);
      if (treeWalk == null) {
        throw new IllegalStateException(
            String.format("TreeWalk is null for commitId %s and path %s".format(commitId, path)));
      }
      // Index 0 should have file data
      byte[] data = objectReader.open(treeWalk.getObjectId(0)).getBytes();
      return new String(data, UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (objectReader != null) {
        objectReader.close();
      }
    }
  }
}
