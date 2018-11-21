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

package com.google.startupos.tools.reviewer.job.impl;

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.CommandLine;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreClientFactory;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.ReviewerProtos.CIRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CIResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.tools.reviewer.job.tasks.Task;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class CiTask extends FirestoreTaskBase implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  // we acquire a lock to process single CIRequest at a time
  private final ReentrantLock lock = new ReentrantLock();

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;

  private static String CI_REQUESTS_PATH = "/reviewer/ci/requests";
  private static String CI_RESPONSES_PATH = "/reviewer/ci/responses";

  @Inject
  public CiTask(
      FileUtils fileUtils,
      GitRepoFactory gitRepoFactory,
      FirestoreClientFactory firestoreClientFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
    this.firestoreClientFactory = firestoreClientFactory;
  }

  @Override
  public Boolean shouldRun() {
    return !lock.isLocked();
  }

  @Override
  public void run() {
    if (lock.tryLock()) {
      try {
        initializeFirestoreClientIfNull();

        log.atInfo().log("Running CI job (Firestore client @ %s)", firestoreClient);

        CIRequest request =
            (CIRequest) this.firestoreClient.popDocument(CI_REQUESTS_PATH, CIRequest.newBuilder());

        if (request == null) {
          // there is no request to process
          return;
        }

        if (!fileUtils.folderExists("ci")) {
          fileUtils.mkdirs("ci");
        }

        CIResponse.Builder responseBuilder = CIResponse.newBuilder().setRequest(request);

        for (CIRequest.Target target : request.getTargetList()) {
          Repo repo = target.getRepo();

          String repoPath =
              fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "ci", repo.getId());

          GitRepo gitRepo = this.gitRepoFactory.create(repoPath);

          try {
            if (fileUtils.folderEmptyOrNotExists(repoPath)) {
              gitRepo.cloneRepo(repo.getUrl(), repoPath);
            } else {
              gitRepo.pull();
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          gitRepo.switchBranch(target.getCommitId());

          log.atInfo().log("Running reviewer-ci.sh in %s", repoPath);
          CommandLine.CommandResult result =
              CommandLine.runCommandForError(
                  "/usr/bin/env bash " + fileUtils.joinPaths(repoPath, "reviewer-ci.sh"));

          responseBuilder.addResults(
              CIResponse.TargetResult.newBuilder()
                  .setTarget(target)
                  .setSuccess(result.exitValue == 0)
                   // TODO: Firestore's limit is 1MB. Truncate log to 950Kb, to leave room for the rest of the message.
                  .setLog(result.stderr)
                  .build());
        }

        firestoreClient.createDocument(CI_RESPONSES_PATH, responseBuilder.build());
      } finally {
        lock.unlock();
      }
    }
  }
}

