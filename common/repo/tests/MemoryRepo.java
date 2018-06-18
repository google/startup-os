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

package com.google.startupos.common.repo.tests;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.repo.Protos;
import com.google.startupos.common.repo.Repo;
import com.google.startupos.common.repo.Protos.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * A memory repo used for testing.
 */
public class MemoryRepo implements Repo {
  private enum ActionOverride {
    NONE,
    SUCCESS,
    FAIL
  }

  class Commit {
    Commit(String id, String message, List<File> files) {
      this.id = id;
      this.message = message;
      this.files = files;
    }

    final String id;
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

  private Map<String, Branch> branches = new HashMap<>();
  private String currentBranch;
  private int nextId;
  private ActionOverride nextPull;
  private ActionOverride nextPush;
  private ActionOverride nextMerge;

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

  public void tagHead(String name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public ImmutableList<Protos.Commit> getCommits(String branch) {
    ImmutableList.Builder<Protos.Commit> result = new ImmutableList.Builder<>();
    for (Commit commit : branches.get(branch).commits.values()) {
      result.add(Protos.Commit.newBuilder().setId(commit.id).addAllFile(commit.files).build());
    }
    return result.build();
  }

  public ImmutableList<File> getUncommittedFiles() {
    // TODO: Implement using FileSystem injection
    throw new UnsupportedOperationException("Not implemented");
  }

  public ImmutableList<File> getFilesInCommit(String commitId) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public Protos.Commit commit(ImmutableList<File> files, String message) {
    String id = String.valueOf(nextId);
    branches.get(currentBranch).commits.put(id, new Commit(id, message, files));
    nextId++;
    return Protos.Commit.newBuilder().setId(id).addAllFile(files).build();
  }

  public void pushAll() {
    for (Branch branch : branches.values()) {
      for (Commit commit : branch.commits.values()) {
        commit.isPushed = true;
      }
    }
  }

  public void pull() {
    // Do nothing
  }

  public boolean merge(String branch) {
    return nextMerge != ActionOverride.FAIL;
  }

  public boolean isMerged(String branch) {
    return branches.get(branch).isMerged;
  }

  @Override
  public void reset(String ref) {
    // TODO: implement if needed
  }

  @Override
  public void removeBranch(String branch) {
    branches.remove(branch);
  }

  @Override
  public ImmutableList<String> listBranches() {
    return ImmutableList.copyOf(branches.keySet());
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

  public String getFileContents(String commitId, String path) {
    return "File contents";
  }
}
