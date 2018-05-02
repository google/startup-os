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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
    throw new UnsupportedOperationException("Not implemented");
  }

  public ImmutableList<Commit> getCommits(String branch) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public ImmutableList<File> getUncommittedFiles() {
    throw new UnsupportedOperationException("Not implemented");
  }

  public Commit commit(ImmutableList<File> files, String message) {
    try {
      for (File file : files) {
        jGit.add().addFilepattern(file.getFilename()).call();
      }
      RevCommit revCommit = jGit.commit().setMessage(message).call();
      return Commit.newBuilder()
          .setId(revCommit.getId().toString())
          .addAllFile(files)
          .build();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void pushAll() {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void pullAll() {
    throw new UnsupportedOperationException("Not implemented");
  }

  public boolean merge(String branch) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public boolean isMerged(String branch) {
    throw new UnsupportedOperationException("Not implemented");
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
