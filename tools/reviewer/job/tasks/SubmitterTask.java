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
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.MessageWithId;
import com.google.startupos.tools.reviewer.ReviewerProtos.CIRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CIResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.tools.reviewer.job.tasks.Task;

import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SubmitterTask extends FirestoreTaskBase implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  private static String DIFF_PATH = "/reviewer/data/diff";
  private static String CI_REQUESTS_PATH = "/reviewer/ci/requests";
  private static String CI_RESPONSES_HISTORY_PATH = "/reviewer/ci/responses/%d/history";

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private final ReentrantLock lock = new ReentrantLock();
  private final HashSet<CIRequest> ciSubmittedDiffIds = new HashSet<>();

  @Inject
  public SubmitterTask(FileUtils fileUtils, GitRepoFactory gitRepoFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
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
        // noinspection unchecked
        List<Diff> diffs =
            (List) this.firestoreClient.getProtoDocuments(DIFF_PATH, Diff.newBuilder());

        if (!fileUtils.folderExists("submitter")) {
          fileUtils.mkdirs("submitter");
        }

        diffs
            .stream()
            // get Diffs in SUBMITTING state
            .filter(diff -> diff.getStatus().equals(Diff.Status.SUBMITTING))
            // for each of them, get latest CIResponse
            .map(
                diff -> {
                  MessageWithId responseWithId =
                      this.firestoreClient.getDocumentFromCollection(
                          String.format(CI_RESPONSES_HISTORY_PATH, diff.getId()),
                          CIResponse.newBuilder());
                  CIResponse response = (CIResponse) responseWithId.message();
                  return Pair.of(diff, response);
                })
            // only proceed if CIResponse was present
            .filter(diffCiPair -> diffCiPair.getValue() != null)
            // only proceed if TargetResult.success = true for all Results
            .filter(
                diffCiPair ->
                    diffCiPair
                        .getValue()
                        .getResultsList()
                        .stream()
                        .allMatch(CIResponse.TargetResult::getSuccess))
            .forEach(
                diffCiPair -> {
                  Diff diff = diffCiPair.getKey();
                  CIResponse response = diffCiPair.getValue();

                  boolean shouldPush = true;
                  List<GitRepo> gitRepos = new ArrayList<>();

                  CIRequest.Builder newRequestBuilder =
                      CIRequest.newBuilder().setForSubmission(true);

                  for (CIResponse.TargetResult targetResult : response.getResultsList()) {
                    CIRequest.Target target = targetResult.getTarget();

                    Repo repo = target.getRepo();
                    String repoPath =
                        fileUtils.joinPaths(
                            fileUtils.getCurrentWorkingDirectory(), "submitter", repo.getId());
                    GitRepo gitRepo = this.gitRepoFactory.create(repoPath);
                    gitRepos.add(gitRepo);

                    try {
                      if (fileUtils.folderEmptyOrNotExists(repoPath)) {
                        gitRepo.cloneRepo(repo.getUrl(), repoPath);
                      } else {
                        gitRepo.pull();
                      }
                    } catch (IOException e) {
                      e.printStackTrace();
                    }

                    String branch = String.format("origin/D%d", diff.getId());
                    gitRepo.switchBranch(branch);

                    newRequestBuilder.addTarget(
                        CIRequest.Target.newBuilder()
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
                      // it means we'll need to later push a new CIRequest
                      shouldPush = false;
                    }
                  }

                  if (shouldPush) {
                    boolean allPushesSuccessful =
                        gitRepos.stream().allMatch(repo -> repo.push("master"));
                    if (allPushesSuccessful) {
                      diff = diff.toBuilder().setStatus(Diff.Status.SUBMITTED).build();
                      this.firestoreClient.setProtoDocument(
                          DIFF_PATH, String.valueOf(diff.getId()), diff);
                      // TODO: store `SubmitterMergeResult`
                    } else {
                      throw new RuntimeException("Not all pushes were successful");
                    }
                  } else {
                    CIRequest newRequest = newRequestBuilder.build();
                    if (!ciSubmittedDiffIds.contains(newRequest)) {
                      this.firestoreClient.setProtoDocument(
                          CI_REQUESTS_PATH, String.valueOf(diff.getId()), newRequest);
                      ciSubmittedDiffIds.add(newRequest);
                    }
                  }
                });

      } finally {
        lock.unlock();
      }
    }
  }
}

