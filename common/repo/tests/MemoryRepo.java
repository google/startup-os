package com.google.startupos.common.repo.tests;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.tools.reviewer.service.Protos.File;

/*
 * A memory repo used for testing.
 */
public class MemoryRepo implements Repo {
  public enum ActionOverride {
    NONE, SUCCESS, FAIL
  }
  class Commit {
    Commit(String id, String message, List<File> files) {
      this.id = id;
      this.message = message;
      this.files = files;
    }
    String id;
    String message;
    boolean isPushed;
    List<File> files = new ArrayList<>();
  }
  class Branch {
    Branch(String name) {
      this.name = name;
    }
    String name;
    boolean isMerged;
    LinkedHashMap<String, Commit> commits = new LinkedHashMap<>();
  }

  Map<String, Branch> branches = new HashMap<String, Branch>();
  String currentBranch;
  int nextId;
  ActionOverride nextPull;
  ActionOverride nextPush;
  ActionOverride nextMerge;

  public MemoryRepo() {
    currentBranch = "master";
    branches.put(currentBranch, new Branch(currentBranch));
  }

  public void switchBranch(String branch) {
    if (!branches.containsKey(branch)) {
      branches.put(branch, new Branch(branch));
    }
    currentBranch = branch;
  }

  public ImmutableList<Protos.Commit> getCommits(String branch) {
    ImmutableList.Builder<Protos.Commit> result = new ImmutableList.Builder<Protos.Commit>();
    for (Commit commit : branches.get(branch).commits.values()) {
      result.add(
          Protos.Commit.newBuilder()
              .setId(commit.id)
              .addAllFile(commit.files)
              .build());
    }
    return result.build();
  }

  public ImmutableList<File> getUncommittedFiles() {
    // TODO: Implement using FileSystem injection
    throw new UnsupportedOperationException("Not implemented");
  }

  public Protos.Commit commit(ImmutableList<File> files, String message) {
    String id = String.valueOf(nextId);
    branches.get(currentBranch).commits.put(id, new Commit(id, message, files));
    nextId++;
    return Protos.Commit.newBuilder()
        .setId(id)
        .addAllFile(files)
        .build();
  }

  public void pushAll() {
    for (Branch branch : branches.values()) {
      for (Commit commit : branch.commits.values()) {
        commit.isPushed = true;
      }
    }
  }

  public void pullAll() {
    // Do nothing
  }

  public boolean merge(String branch) {
    if (nextMerge == ActionOverride.FAIL) {
      return false;
    }
    return true;
  }

  public boolean isMerged(String branch) {
    return branches.get(branch).isMerged;
  }

  public void succeedNextPull() {
    nextPull = ActionOverride.SUCCESS;
  }

  public void succeedNextPush() {
    nextPush = ActionOverride.SUCCESS;
  }

  public void succeedNextMerge() {
    nextMerge = ActionOverride.SUCCESS;
  }

  public void failNextPull() {
    nextPull = ActionOverride.FAIL;
  }

  public void failNextPush() {
    nextPush = ActionOverride.FAIL;
  }

  public void failNextMerge() {
    nextMerge = ActionOverride.FAIL;
  }

  public String getFileContents(File file) {
    return "File contents";
  }  
}
