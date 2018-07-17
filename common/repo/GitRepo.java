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

// TODO: Implement methods
@AutoFactory
public class GitRepo implements Repo {
  private final List<String> gitCommandBase;
  private final List<CommandResult> commandLog = new ArrayList<>();

  GitRepo(@Provided FileUtils fileUtils, String repoPath) {
    gitCommandBase =
        Arrays.asList(
            "git", "--git-dir=" + fileUtils.joinPaths(repoPath, ".git"), "--work-tree=" + repoPath);
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
    CommandResult result = new CommandResult();
    try {
      List<String> fullCommand = new ArrayList<>(gitCommandBase);
      if (!command.contains("\"")) {
        fullCommand.addAll(Arrays.asList(command.split(" ")));
      } else {
        List<String> intermediateSeparation = new ArrayList<>();
        intermediateSeparation.add(command.substring(0, command.indexOf("\"")));
        intermediateSeparation.add(
            command.substring(command.indexOf("\""), command.lastIndexOf("\"") + 1));
        intermediateSeparation.add(
            command.substring(command.lastIndexOf("\""), command.length() - 1));
        for (String substring : intermediateSeparation) {
          if (substring.length() == 0) {
            continue;
          }
          if (substring.startsWith("\"")) {
            fullCommand.add(substring);
          } else {
            fullCommand.addAll(Arrays.asList(substring.split(" ")));
          }
        }
      }
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
    runCommand("checkout --quiet -B " + branch);
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
    CommandResult commandResult = runCommand("log --pretty=%H " + branch);
    // We reverse to return by chronological order
    return splitLines(commandResult.stdout).reverse();
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
    CommandResult commandResult = runCommand("status --short");
    ImmutableList<String> lines = splitLines(commandResult.stdout);
    for (String line : lines) {
      String[] parts;
      if (!line.trim().startsWith("M")
          || !line.trim().startsWith("RM")
          || !line.trim().startsWith("??")) {
        line = line.replaceFirst("\\s{2}", " ");
      }
      parts = line.trim().split(" ");

      File.Builder fileBuilder = File.newBuilder();
      fileBuilder.setAction(getAction(parts[0]));
      fileBuilder.setFilename(
          fileBuilder.getAction().equals(File.Action.RENAME) ? parts[3] : parts[1]);
      files.add(fileBuilder.build());
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
        return File.Action.UNTRACKED;
      default:
        throw new IllegalStateException("Unknown change type " + changeType);
    }
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
    runCommand("commit -m \"" + message + "\"");
    String commitId = getHeadCommitId();
    for (File file : files) {
      commitBuilder.addFile(file.toBuilder().setCommitId(commitId));
    }
    return commitBuilder.setId(commitId).build();
  }

  @Override
  public void pushAll() {
    runCommand("push --all origin");
  }

  @Override
  public void pull() {
    runCommand("pull");
  }

  @Override
  public boolean merge(String branch) {
    return merge(branch, false);
  }

  @Override
  public boolean mergeTheirs(String branch) {
    switchToMasterBranch();
    CommandResult commandResult = runCommand("merge " + branch + " -s recursive -Xtheirs");
    String err = commandResult.stderr;
    return err.length() == 0;
  }

  public boolean merge(String branch, boolean remote) {
    switchToMasterBranch();
    CommandResult commandResult;
    if (remote) {
      runCommand("fetch origin " + branch);
      commandResult =
          runCommand(
              "merge origin/"
                  + branch
                  + " -m Merge_remote-tracking_branch_\'origin/"
                  + branch
                  + "\'");
    } else {
      commandResult = runCommand("merge " + branch);
    }
    String err = commandResult.stderr;
    return err.length() == 0;
  }

  @Override
  public boolean isMerged(String branch) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void reset(String ref) {
    // HEAD pointer would be
    // reset to `ref` and all changes introduced after it
    // would be marked as unstaged but saved in working tree
    runCommand("reset " + ref);
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

  @Override
  public String getFileContents(String commitId, String path) {
    CommandResult commandResult = runCommand("show " + commitId + ":" + path);
    String result = commandResult.stdout;
    int lastIndexOfNewLineSymbol = result.lastIndexOf("\n");
    if (lastIndexOfNewLineSymbol >= 0) {
      result =
          new StringBuilder(result)
              .replace(lastIndexOfNewLineSymbol, lastIndexOfNewLineSymbol + 1, "")
              .toString();
    }
    return result;
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
    return commandResult.stdout.replace("\n", "");
  }

  private void switchToMasterBranch() {
    if (!currentBranch().equals("master")) {
      System.out.println("You are not in the branch master");
      System.out.println("Switching to the master branch...");
      switchBranch("master");
    }
  }

  @VisibleForTesting
  public ImmutableList<String> getTagList() {
    ImmutableList.Builder<String> result = ImmutableList.builder();
    CommandResult commandResult = runCommand("tag");
    try {
      ImmutableList<String> lines = splitLines(commandResult.stdout);
      for (String line : lines) {
        result.add(line);
      }
    } catch (IllegalStateException e) {
      throw new IllegalStateException("Get tag list failed!");
    }
    return result.build();
  }

  @VisibleForTesting
  public void setFakeUsersData() {
    runCommand("config user.email \"test@test.test\"");
    runCommand("config user.name \"test\"");
  }
}

