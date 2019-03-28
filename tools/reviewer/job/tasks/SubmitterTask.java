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

package com.google.startupos.tools.reviewer.job.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.firestore.ProtoChange;
import com.google.startupos.common.firestore.ProtoEventListener;
import com.google.startupos.common.firestore.ProtoQuerySnapshot;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.ReviewerConstants;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse.TargetResult.Status;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.tools.reviewer.local_server.service.Protos.Diff;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class SubmitterTask implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private final ReentrantLock lock = new ReentrantLock();
  private final HashSet<CiRequest> ciSubmittedDiffIds = new HashSet<>();
  protected FirestoreProtoClient firestoreClient;
  private boolean listenerRegistered = false;
  ImmutableList<Diff> diffs = ImmutableList.of();

  @Inject
  public SubmitterTask(
      FileUtils fileUtils, GitRepoFactory gitRepoFactory, FirestoreProtoClient firestoreClient) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
    this.firestoreClient = firestoreClient;
  }

  @Override
  public Boolean shouldRun() {
    return !lock.isLocked();
  }

  public void tryRegisterListener() {
    if (!listenerRegistered) {
      firestoreClient.addCollectionListener(
          ReviewerConstants.DIFF_COLLECTION,
          Diff.newBuilder(),
          new ProtoEventListener<ProtoQuerySnapshot<Diff>>() {
            @Override
            public void onEvent(
                @Nullable ProtoQuerySnapshot<Diff> snapshot, @Nullable RuntimeException e) {
              if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
              }
              ArrayList<Diff> updatedDiffs = new ArrayList(diffs);
              log.atInfo().log("Received %d changes", snapshot.getProtoChanges().size());
              for (ProtoChange<Diff> protoChange : snapshot.getProtoChanges()) {
                switch (protoChange.getType()) {
                  case ADDED:
                    updatedDiffs.add(protoChange.getProto());
                    break;
                  case MODIFIED:
                    // The indices are made so that this type of sequential remove & add works, see:
                    // https://googleapis.github.io/google-cloud-java/google-cloud-clients/apidocs/com/google/cloud/firestore/DocumentChange.html#getNewIndex--
                    updatedDiffs.remove(protoChange.getOldIndex());
                    updatedDiffs.add(protoChange.getNewIndex(), protoChange.getProto());
                    break;
                  case REMOVED:
                    updatedDiffs.remove(protoChange.getOldIndex());
                    break;
                  default:
                    throw new IllegalStateException("Unknown enum " + protoChange.getType());
                }
              }
              diffs = ImmutableList.copyOf(updatedDiffs);
              run();
            }
          });
      listenerRegistered = true;
    }
  }

  @Override
  public void run() {
    if (lock.tryLock()) {
      try {
        tryRegisterListener();

        if (!fileUtils.folderExists("submitter")) {
          fileUtils.mkdirs("submitter");
        }

        diffs.stream()
            // Get Diffs that:
            // - Are in SUBMITTING state
            // - Have at least one CiResponse
            // - The last (latest) CiResponse has passed all targets
            .filter(
                diff ->
                    diff.getStatus().equals(Diff.Status.SUBMITTING)
                        && !diff.getCiResponseList().isEmpty()
                        && diff.getCiResponse(diff.getCiResponseCount() - 1).getResultList()
                            .stream()
                            .allMatch(x -> x.getStatus() == Status.SUCCESS))
            .forEach(diff -> trySubmitDiff(diff));
      } finally {
        lock.unlock();
      }
    }
  }

  private void trySubmitDiff(Diff diff) {
    log.atInfo().log("Attempting to submit diff %d", diff.getId());

    CiResponse response = diff.getCiResponse(diff.getCiResponseCount() - 1);
    boolean shouldPush = true;
    List<GitRepo> gitRepos = new ArrayList<>();

    CiRequest.Builder newRequestBuilder = CiRequest.newBuilder().setForSubmission(true);
    for (CiResponse.TargetResult targetResult : response.getResultList()) {
      CiRequest.Target target = targetResult.getTarget();

      Repo repo = target.getRepo();
      String repoPath =
          fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "submitter", repo.getId());
      GitRepo gitRepo = gitRepoFactory.create(repoPath);
      gitRepos.add(gitRepo);
      try {
        if (fileUtils.folderEmptyOrNotExists(repoPath)) {
          gitRepo.cloneRepo(repo.getUrl(), repoPath);
        } else {
          gitRepo.pull();
        }
      } catch (Exception e) {
        log.atSevere().withCause(e).log("Failed to clone or pull repo at %s", repoPath);
        return;
      }
      String branch = String.format("origin/D%d", diff.getId());
      gitRepo.switchBranch(branch);

      newRequestBuilder.addTarget(
          CiRequest.Target.newBuilder()
              .setRepo(repo)
              .setCommitId(gitRepo.getHeadCommitId())
              .build());

      if (gitRepo.getHeadCommitId().equals(target.getCommitId())) {
        // latest result for CI matches last commit on the branch

        gitRepo.switchBranch("master");
        boolean mergingResult = gitRepo.merge("D%d", true);
        if (!mergingResult || gitRepo.hasChanges("master")) {
          shouldPush = false;
        }

      } else {
        // commit on the branch is newer than results in CI
        // it means we'll need to later push a new CiRequest
        shouldPush = false;
      }
    }

    if (shouldPush) {
      boolean allPushesSuccessful = gitRepos.stream().allMatch(repo -> repo.push("master"));
      if (allPushesSuccessful) {
        log.atInfo().log("All repos pushed successfully");
        diff = diff.toBuilder().setStatus(Diff.Status.SUBMITTED).build();
        firestoreClient.setProtoDocument(
            ReviewerConstants.DIFF_COLLECTION, String.valueOf(diff.getId()), diff);
        // TODO: store `SubmitterMergeResult`
      } else {
        // TODO: find out which one.
        log.atSevere().log("Some repo pushes failed");
      }
    } else {
      // TODO: Find out which repo caused it.
      log.atInfo().log("Latest CiResult is outdated.");
      CiRequest newRequest = newRequestBuilder.build();
      if (!ciSubmittedDiffIds.contains(newRequest)) {
        log.atInfo().log("New CiRequest sent.");
        firestoreClient.setProtoDocument(
            ReviewerConstants.CI_REQUESTS_PATH, String.valueOf(diff.getId()), newRequest);
        ciSubmittedDiffIds.add(newRequest);
      } else {
        log.atInfo().log("CiRequest was already sent before. Doing nothing.");
      }
    }
  }
}

