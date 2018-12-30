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
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;


public class CiTask implements Task {
  // Firestore's message limit is 1MB, so we don't want to pass that.
  // Protobin gets translated to Base64, which can increase size by roughly 30%.
  // So our limit is ~700KB. We limit the log below that to leave room for multiple CiResponses.
  private static final int MAX_LOG_LENGTH = 15 * 1024;

  private static FluentLogger log = FluentLogger.forEnclosingClass();

  // We acquire a lock to process a single CiRequest at a time.
  private final ReentrantLock lock = new ReentrantLock();

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  protected FirestoreProtoClient firestoreClient;

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

  @Override
  public void run() {
    CiResponse.Builder responseBuilder = CiResponse.newBuilder();
    MessageWithId requestWithDiffId = null;
    if (lock.tryLock()) {
      try {
        requestWithDiffId =
            firestoreClient.getDocumentFromCollection(
                ReviewerConstants.CI_REQUESTS_PATH, CiRequest.newBuilder());
        if (requestWithDiffId == null) {
          // There is no request to process
          return;
        }

        CiRequest request = (CiRequest) requestWithDiffId.message();
        System.out.println("Processing request:\n" + request);

        if (!fileUtils.folderExists("ci")) {
          fileUtils.mkdirs("ci");
        }

        responseBuilder = CiResponse.newBuilder().setRequest(request);

        for (CiRequest.Target target : request.getTargetList()) {
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
          } catch (IOException e) {
            e.printStackTrace();
            responseBuilder =
                responseBuilder.addResults(
                    CiResponse.TargetResult.newBuilder()
                        .setTarget(target)
                        .setStatus(Status.FAIL)
                        .setLog(e.toString())
                        .build());
            return;
          }
          gitRepo.switchBranch(target.getCommitId());

          log.atInfo().log("Running reviewer-ci.sh in %s", repoPath);
          CommandLine.CommandResult result =
              CommandLine.runCommandForError(
                  "/usr/bin/env bash " + fileUtils.joinPaths(repoPath, "reviewer-ci.sh"), repoPath);
          String log = result.stderr;
          if (log.length() > MAX_LOG_LENGTH) {
            log =
                log.substring(0, MAX_LOG_LENGTH)
                    + "[log truncated from length "
                    + log.length()
                    + "]\n";
          }
          responseBuilder.addResults(
              CiResponse.TargetResult.newBuilder()
                  .setTarget(target)
                  .setStatus(result.exitValue == 0 ? Status.SUCCESS : Status.FAIL)
                  .setLog(log)
                  .build());
          CiResponse ciResponse = responseBuilder.build();
          // XXX: Tell Vadim about s/CIResponse/CiResponse
          Diff diff =
              (Diff)
                  firestoreClient.getProtoDocument(
                      ReviewerConstants.DIFF_COLLECTION,
                      String.valueOf(request.getDiffId()),
                      Diff.newBuilder());
          firestoreClient.setProtoDocument(
              ReviewerConstants.DIFF_COLLECTION,
              String.valueOf(request.getDiffId()),
              diff.toBuilder().addCiResponse(ciResponse).build());
          firestoreClient.addProtoDocumentToCollection(
              String.format(ReviewerConstants.CI_RESPONSES_PATH, requestWithDiffId.id()),
              ciResponse);
        }
      } finally {
        lock.unlock();
      }
    }
  }
}

