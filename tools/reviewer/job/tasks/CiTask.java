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

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.CommandLine;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.firestore.MessageWithId;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.ReviewerConstants;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse.TargetResult.Status;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.firestore.ProtoEventListener;
import com.google.startupos.common.firestore.ProtoQuerySnapshot;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import com.google.startupos.tools.reviewer.ReviewerConstants;
import com.google.startupos.common.firestore.ProtoChange;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;

public class CiTask implements Task {
  // Firestore's message limit is 1MB, so we don't want to pass that.
  // Protobin gets translated to Base64, which can increase size by roughly 30%.
  // So our limit is ~700KB. We limit the log below that to leave room for multiple CiResponses.
  private static final int MAX_LOG_LENGTH = 15 * 1024;

  private static FluentLogger log = FluentLogger.forEnclosingClass();

  // We acquire a lock to make sure we run a single CiTask. Note that CiTask currently blocks on
  // CiRequests, so it's all sequential.
  private final ReentrantLock lock = new ReentrantLock();

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  protected FirestoreProtoClient firestoreClient;
  private boolean listenerRegistered = false;
  ImmutableList<CiRequest> requests = ImmutableList.of();

  @Inject
  public CiTask(
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
          ReviewerConstants.CI_REQUESTS_PATH,
          CiRequest.newBuilder(),
          new ProtoEventListener<ProtoQuerySnapshot<CiRequest>>() {
            @Override
            public void onEvent(
                @Nullable ProtoQuerySnapshot<CiRequest> snapshot, @Nullable RuntimeException e) {
              if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
              }
              ArrayList<CiRequest> updatedRequests = new ArrayList(requests);
              for (ProtoChange<CiRequest> protoChange : snapshot.getProtoChanges()) {
                switch (protoChange.getType()) {
                  case ADDED:
                    updatedRequests.add(protoChange.getProto());
                    break;
                  case MODIFIED:
                    // The indices are made so that this type of sequential remove & add works, see:
                    // https://googleapis.github.io/google-cloud-java/google-cloud-clients/apidocs/com/google/cloud/firestore/DocumentChange.html#getNewIndex--
                    updatedRequests.remove(protoChange.getOldIndex());
                    updatedRequests.add(protoChange.getNewIndex(), protoChange.getProto());
                    break;
                  case REMOVED:
                    updatedRequests.remove(protoChange.getOldIndex());
                    break;
                }
              }
              requests = ImmutableList.copyOf(updatedRequests);
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
        if (requests.isEmpty()) {
          // There is no request to process
          return;
        }
        for (CiRequest request : requests) {
          processRequest(request);
        }
      } finally {
        lock.unlock();
      }
    }
  }

  private void processRequest(CiRequest request) {
    log.atInfo().log("Processing request:\n%s", request);
    CiResponse.Builder responseBuilder = CiResponse.newBuilder().setRequest(request);
    if (!fileUtils.folderExists("ci")) {
      fileUtils.mkdirs("ci");
    }

    for (CiRequest.Target target : request.getTargetList()) {
      log.atInfo().log("Processing target:\n%s", target);
      Repo repo = target.getRepo();
      String repoPath =
          fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "ci", repo.getId());
      GitRepo gitRepo = this.gitRepoFactory.create(repoPath);
      try {
        if (fileUtils.folderEmptyOrNotExists(repoPath)) {
          gitRepo.cloneRepo(repo.getUrl(), repoPath);
        } else {
          gitRepo.switchBranch("master");
          gitRepo.pull();
        }
      } catch (Exception e) {
        log.atSevere().withCause(e).log("Failed to process target");
        responseBuilder =
            responseBuilder.addResults(
                CiResponse.TargetResult.newBuilder()
                    .setTarget(target)
                    .setStatus(Status.FAIL)
                    .setLog(e.toString())
                    .build());
        saveCiResponse(responseBuilder.build(), request.getDiffId());
        return;
      }
      gitRepo.switchBranch(target.getCommitId());

      log.atInfo().log("Running reviewer-ci.sh in %s", repoPath);
      CommandLine.CommandResult result =
          CommandLine.runCommandForError(
              "/usr/bin/env bash " + fileUtils.joinPaths(repoPath, "reviewer-ci.sh"), repoPath);
      log.atInfo().log("Done running reviewer-ci.sh");
      String ciLog = result.stderr;
      if (ciLog.length() > MAX_LOG_LENGTH) {
        ciLog =
            ciLog.substring(0, MAX_LOG_LENGTH)
                + "[log truncated from length "
                + ciLog.length()
                + "]\n";
      }
      Status status = result.exitValue == 0 ? Status.SUCCESS : Status.FAIL;
      responseBuilder.addResults(
          CiResponse.TargetResult.newBuilder()
              .setTarget(target)
              .setStatus(status)
              .setLog(ciLog)
              .build());
      saveCiResponse(responseBuilder.build(), request.getDiffId());
      firestoreClient.deleteDocument(
          ReviewerConstants.CI_REQUESTS_PATH, Long.toString(request.getDiffId()));
      log.atInfo().log("Done processing CiRequest. Status=" + status);
    }
  }

  public void saveCiResponse(CiResponse ciResponse, long diffId) {
    Diff diff =
        (Diff)
            firestoreClient.getProtoDocument(
                ReviewerConstants.DIFF_COLLECTION, String.valueOf(diffId), Diff.newBuilder());
    firestoreClient.setProtoDocument(
        ReviewerConstants.DIFF_COLLECTION,
        String.valueOf(diffId),
        diff.toBuilder().addCiResponse(ciResponse).build());
    firestoreClient.addProtoDocumentToCollection(
        String.format(ReviewerConstants.CI_RESPONSES_PATH, diffId), ciResponse);
  }
}

