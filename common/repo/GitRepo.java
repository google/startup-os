package com.google.startupos.common.repo;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.tools.reviewer.service.Protos.File;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.nio.file.Paths;
import java.io.IOException;
import org.eclipse.jgit.revwalk.RevCommit;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import java.nio.file.FileSystem;


// TODO: Implement methods
@AutoFactory
public class GitRepo implements Repo {
  private Git jGit;

  GitRepo(@Provided FileSystem fileSystem, String repoPath) {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try {
      Repository jGitRepo =
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

  public ImmutableList<File> getUncomittedFiles() {
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

  public String getFileContents(File file) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
