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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.Protos.Commit;
import com.google.startupos.common.repo.Protos.File;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AutoFactory
public class GitRepo implements Repo {
  private final List<String> gitCommandBase;
  private final List<CommandResult> commandLog = new ArrayList<>();
  private final FileUtils fileUtils;

  GitRepo(@Provided FileUtils fileUtils, String repoPath) {
    this.fileUtils = fileUtils;
    gitCommandBase =
        Arrays.asList(
            "git",
            "--git-dir=" + fileUtils.joinToAbsolutePath(repoPath, ".git"),
            "--work-tree=" + repoPath);
  }

  private class CommandResult {
    private String command;
    private String stdout;
    private String stderr;
  }

  private String readLines(InputStream inputStream) throws IOException {
    StringBuffer output = new StringBuffer();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append('\n');
      }
    }
    return output.toString();
  }

  private CommandResult runCommand(String command) {
    return runCommand(Arrays.asList(command.split(" ")));
  }

  private CommandResult runCommand(String command, boolean throwException) {
    return runCommand(Arrays.asList(command.split(" ")), throwException);
  }

  private CommandResult runCommand(List<String> command) {
    return runCommand(command, true);
  }

  private CommandResult runCommand(List<String> command, boolean throwException) {
    CommandResult result = new CommandResult();
    try {
      List<String> fullCommand = new ArrayList<>(gitCommandBase);
      fullCommand.addAll(command);
      String[] fullCommandArray = fullCommand.toArray(new String[0]);
      result.command = String.join(" ", fullCommand);
      Process process = Runtime.getRuntime().exec(fullCommandArray);
      result.stdout = readLines(process.getInputStream());
      result.stderr = readLines(process.getErrorStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (!result.stderr.isEmpty() && throwException) {
      throw new RuntimeException(formatError(result));
    }
    commandLog.add(result);
    return result;
  }

  private String formatError(CommandResult commandResult) {
    StringBuilder result = new StringBuilder();
    result.append(String.format("\n%s\n%s", commandResult.command, commandResult.stderr));
    if (!commandLog.isEmpty()) {
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

  @Override
  public void switchBranch(String branch) {
    if (branchExists(branch)) {
      runCommand("checkout --quiet " + branch);
    } else {
      runCommand("checkout --quiet -b " + branch);
    }
  }

  @Override
  public void tagHead(String name) {
    runCommand("tag " + name);
  }

  private ImmutableList<String> splitLines(String string) {
    return ImmutableList.copyOf(
        Arrays.stream(string.split("\\r?\\n"))
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList()));
  }

  public ImmutableList<String> getCommitIds(String branch) {
    if (!branch.startsWith("remotes/origin")) {
      branch = "refs/heads/" + branch;
    }
    CommandResult commandResult = runCommand("log --pretty=%H refs/heads/master.." + branch);
    // We reverse to return by chronological order
    ImmutableList<String> commits = splitLines(commandResult.stdout).reverse();
    // Get last commit on master branch
    commandResult = runCommand("merge-base refs/heads/master " + branch);
    return ImmutableList.<String>builder()
        .addAll(splitLines(commandResult.stdout))
        .addAll(commits)
        .build();
  }

  @Override
  public ImmutableList<Commit> getCommits(String branch) {
    ImmutableList<String> commits = getCommitIds(branch);
    ImmutableList.Builder<Commit> result = ImmutableList.builder();
    for (String commit : commits) {
      if (commit.equals(commits.get(0))) {
        // We don't need the file list for the first commit, which is the last commit from master.
        result.add(Commit.newBuilder().setId(commit).build());
      } else {
        result.add(Commit.newBuilder().setId(commit).addAllFile(getFilesInCommit(commit)).build());
      }
    }
    return result.build();
  }

  @Override
  public ImmutableList<File> getUncommittedFiles() {
    ImmutableList.Builder<File> files = ImmutableList.builder();
    CommandResult commandResult = runCommand("status --porcelain");
    ImmutableList<String> lines = splitLines(commandResult.stdout);
    for (String line : lines) {
      line = line.replaceFirst("  ", " ");
      String[] parts = line.trim().split(" ");
      /* We extract the action and filename from line. Here are some example lines:
      D  deleted.txt
      action:DELETE, filename:deleted.txt
       M modified.txt
      action:MODIFY, filename:modified.txt
      A  package/new.txt
      action:ADD, filename:package/new.txt
      R  old_name.txt -> new_name.txt
      action:RENAME, filename:new_name.txt
      ?? package/untracked.txt
      action:ADD, filename:package/untracked.txt
      */
      File.Action action = getAction(parts[0]);
      if (action.equals(File.Action.RENAME) || action.equals(File.Action.COPY)) {
        String filename = parts[3];
        String originalFilename = parts[1];
        files.add(
            File.newBuilder()
                .setAction(action)
                .setFilename(filename)
                .setOriginalFilename(originalFilename)
                .build());
      } else {
        String filename = parts[1];
        files.add(File.newBuilder().setAction(action).setFilename(filename).build());
      }
    }
    return files.build();
  }

  private File.Action getAction(String changeType) {
    switch (changeType) {
      case "A":
        return File.Action.ADD;
      case "D":
        return File.Action.DELETE;
      case "RM":
        return File.Action.RENAME;
      case "R":
        return File.Action.RENAME;
      case "M":
        return File.Action.MODIFY;
      case "AM":
        return File.Action.COPY;
      case "C":
        return File.Action.COPY;
      case "??":
        return File.Action.ADD;
      default:
        throw new IllegalStateException("Unknown change type " + changeType);
    }
  }

  @Override
  public boolean commitExists(String commitId) {
    CommandResult commandResult = runCommand("cat-file -t " + commitId, false);
    return commandResult.stdout.trim().startsWith("commit");
  }

  @Override
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

  @Override
  public Commit commit(ImmutableList<File> files, String message) {
    Commit.Builder commitBuilder = Commit.newBuilder();
    for (File file : files) {
      addFile(file.getFilename());
    }
    runCommand(Arrays.asList("commit", "-m", "\"" + message + "\""));
    String commitId = getHeadCommitId();
    for (File file : files) {
      commitBuilder.addFile(file.toBuilder().setCommitId(commitId));
    }
    return commitBuilder.setId(commitId).build();
  }

  @Override
  public void push() {
    runCommand("push -q --all --atomic origin");
  }

  @Override
  public void pull() {
    runCommand("pull -q");
  }

  @Override
  public boolean merge(String branch) {
    return merge(branch, false);
  }

  public boolean merge(String branch, boolean remote) {
    switchToMasterBranch();
    CommandResult mergeCommandResult;
    if (remote) {
      CommandResult fetchCommandResult = runCommand("fetch -q origin " + branch);
      if (!fetchCommandResult.stderr.isEmpty()) {
        throw new IllegalStateException(
            "Failed to fetch remote branch before merging \'"
                + branch
                + "\': "
                + fetchCommandResult.stderr);
      }
      mergeCommandResult =
          runCommand(
              "merge origin/"
                  + branch
                  + " -m Merge_remote-tracking_branch_\'origin/"
                  + branch
                  + "\'");
    } else {
      mergeCommandResult = runCommand("merge " + branch);
    }
    return mergeCommandResult.stderr.isEmpty();
  }

  @Override
  public boolean isMerged(String branch) {
    CommandResult commandResult = runCommand("branch --merged master");
    List<String> mergedBranches =
        splitLines(commandResult.stdout)
            .stream()
            .map(line -> line.trim().replaceAll("\\*", ""))
            .collect(Collectors.toList());
    return mergedBranches.contains(branch);
  }

  @Override
  public void reset(String ref) {
    runCommand("reset " + ref);
  }

  @Override
  public void removeBranch(String branch) {
    runCommand("branch --quiet -D " + branch);
  }

  @Override
  public boolean branchExists(String name) {
    // Note: Can also be done directly using 'git rev-parse --verify -q <branch name>'
    return listBranches().contains(name);
  }

  @Override
  public ImmutableList<String> listBranches() {
    CommandResult commandResult = runCommand("branch -a");
    return ImmutableList.copyOf(
        splitLines(commandResult.stdout)
            .stream()
            .map(branch -> branch.substring(2))
            .collect(Collectors.toList()));
  }

  @Override
  public String getFileContents(String commitId, String path) {
    return runCommand("--no-pager show " + commitId + ":" + path).stdout;
  }

  @Override
  public String currentBranch() {
    return runCommand("rev-parse --abbrev-ref HEAD").stdout.trim();
  }

  public void init() {
    runCommand("init");
  }

  public void addFile(String path) {
    runCommand("add " + path);
  }

  public String getHeadCommitId() {
    CommandResult commandResult = runCommand("rev-parse HEAD");
    return commandResult.stdout.trim().replace("\n", "");
  }

  private void switchToMasterBranch() {
    if (!currentBranch().equals("master")) {
      switchBranch("master");
    }
  }

  @VisibleForTesting
  public ImmutableList<String> getTagList() {
    return splitLines(runCommand("tag").stdout);
  }

  @VisibleForTesting
  public void setFakeUsersData() {
    runCommand("config user.email \"test@test.test\"");
    runCommand("config user.name \"test\"");
  }

  public boolean cloneRepo(String url, String path) {
    CommandResult commandResult =
        runCommand("clone -q " + url + " " + fileUtils.joinToAbsolutePath(path, ".git"));
    return commandResult.stderr.isEmpty();
  }
}

