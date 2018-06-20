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
import com.google.common.collect.Lists;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
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
  private List<String> gitCommandBase;
  private List<CommandResult> commandLog = new ArrayList<>();

  GitRepo(@Provided FileUtils fileUtils, String repoPath) {
    gitCommandBase =
        Arrays.asList(
            "git", "--git-dir=" + fileUtils.joinPaths(repoPath, ".git"), "--work-tree=" + repoPath);
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

  class CommandResult {
    String command;
    String stdout;
    String stderr;
  }

  private String readLines(InputStream inputStream) throws IOException {
    StringBuffer output = new StringBuffer();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line = "";
    while ((line = reader.readLine()) != null) {
      output.append(line + "\n");
    }
    return output.toString();
  }

  private CommandResult runCommand(String command) {
    CommandResult result = new CommandResult();
    try {
      List<String> fullCommand = new ArrayList<>(gitCommandBase);
      fullCommand.addAll(Arrays.asList(command.split(" ")));
      String[] fullCommandArray = fullCommand.toArray(new String[0]);
      result.command = String.join(" ", fullCommand);
      Process process = Runtime.getRuntime().exec(fullCommandArray);
      process.waitFor();
      result.stdout = readLines(process.getInputStream());
      result.stderr = readLines(process.getErrorStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (!result.stderr.isEmpty()) {
      throw new RuntimeException(formatError(result));
    }
    commandLog.add(result);
    return result;
  }

  private String formatError(CommandResult commandResult) {
    StringBuilder result = new StringBuilder();
    result.append(String.format("\n%s\n%s", commandResult.command, commandResult.stderr));
    if (commandLog.size() > 0) {
      result.append("Previous git commands (most recent is on top):\n");
      for (CommandResult previousCommand : Lists.reverse(commandLog)) {
        result.append(
            String.format(
                "\n%s\nstdout: %s\nstderr: %s",
                previousCommand.command, previousCommand.stdout, previousCommand.stderr));
      }
    }
    return result.toString();
  }

  public void switchBranch(String branch) {
    runCommand("checkout --quiet -B " + branch);
  }

  public void tagHead(String name) {
    try {
      jGit.tag().setName(name).call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private ImmutableList<String> splitLines(String string) {
    return ImmutableList.copyOf(
        Arrays.stream(string.split("\\r?\\n"))
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList()));
  }

  public ImmutableList<String> getCommitIds(String branch) {
    CommandResult commandResult = runCommand("log --pretty=%H master.." + branch);
    return splitLines(commandResult.stdout);
  }

  public ImmutableList<Commit> getCommits(String branch) {
    ImmutableList<String> commits = getCommitIds(branch);
    ImmutableList.Builder<Commit> result = ImmutableList.builder();
    for (String commit : commits) {
      result.add(Commit.newBuilder().setId(commit).addAllFile(getFilesInCommit(commit)).build());
    }
    return result.build();
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
                      File.newBuilder().setAction(File.Action.ADD).setFilename(untracked).build()));
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return files.build();
  }

  private File.Action getAction(String changeType) {
    switch (changeType) {
      case "A":
        return File.Action.ADD;
      case "D":
        return File.Action.DELETE;
      case "R":
        return File.Action.RENAME;
      case "M":
        return File.Action.MODIFY;
      case "C":
        return File.Action.COPY;
      default:
        throw new IllegalStateException("Unknown change type " + changeType);
    }
  }

  public ImmutableList<File> getFilesInCommit(String commitId) {
    CommandResult commandResult =
        runCommand("diff-tree --no-commit-id --name-status -r " + commitId);
    ImmutableList.Builder<File> result = ImmutableList.builder();
    try {
      ImmutableList<String> lines = splitLines(commandResult.stdout);
      for (String line : lines) {
        String[] parts = line.split("\t");
        File file =
            File.newBuilder()
                .setAction(getAction(parts[0].trim()))
                .setCommitId(commitId)
                .setFilename(parts[1].trim())
                .build();
        result.add(file);
      }
    } catch (IllegalStateException e) {
      throw new IllegalStateException("getFilesInCommit failed for commit " + commitId, e);
    }
    return result.build();
  }

  public Commit commit(ImmutableList<File> files, String message) {
    Commit.Builder commitBuilder = Commit.newBuilder();
    try {
      for (File file : files) {
        jGit.add().addFilepattern(file.getFilename()).call();
      }
      RevCommit revCommit = jGit.commit().setMessage(message).call();
      String commitId = revCommit.toObjectId().name();
      for (File file : files) {
        commitBuilder.addFile(file.toBuilder().setCommitId(commitId));
      }
      return commitBuilder.setId(commitId).build();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void pushAll() {
    try {
      jGit.push().setPushAll().setAtomic(true).call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public void pull() {
    try {
      jGit.pull().call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean merge(String branch, boolean remote) {
    try {
      Ref current = jGitRepo.exactRef(jGitRepo.getFullBranch());
      Ref ref;
      boolean successfulMerge = true;
      if (remote) {
        ref = jGitRepo.exactRef("refs/remotes/origin/" + branch);
      } else {
        ref = jGitRepo.exactRef("refs/heads/" + branch);
      }
      AddCommand addCommand = jGit.add();
      jGit.merge().include(ref).call();
      for (String conflictingFile : jGit.status().call().getConflicting()) {
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

  public boolean merge(String branch) {
    return merge(branch, false);
  }

  @Override
  public boolean mergeTheirs(String branch) {
    try {
      Ref current = jGitRepo.exactRef(jGitRepo.getFullBranch());
      Ref ref = jGitRepo.exactRef("refs/heads/" + branch);
      jGit.merge().setStrategy(MergeStrategy.THEIRS).include(ref).call();
      jGit.reset().setRef(current.getObjectId().toObjectId().name()).call();
      AddCommand add = jGit.add();
      for (String file : jGit.status().call().getUntracked()) {
        add.addFilepattern(file);
      }
      try {
        add.call();
        jGit.commit().setAll(true).setMessage("Updated changes").call();
        return true;
      } catch (NoFilepatternException e) {
        // Nothing to commit
        return true;
      }
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
    runCommand("branch --quiet -D " + branch);
  }

  @Override
  public ImmutableList<String> listBranches() {
    CommandResult commandResult = runCommand("branch");
    return ImmutableList.copyOf(
        splitLines(commandResult.stdout)
            .stream()
            .map(branch -> branch.substring(2))
            .collect(Collectors.toList()));
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

  public void init() {
    runCommand("init");
  }

  public String currentBranch() {
    return runCommand("rev-parse --abbrev-ref HEAD").stdout;
  }
}
