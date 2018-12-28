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
import com.google.startupos.tools.reviewer.ReviewerProtos.CiRequest;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse;
import com.google.startupos.tools.reviewer.ReviewerProtos.CiResponse.TargetResult.Status;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.tools.reviewer.job.tasks.Task;
import com.google.startupos.common.firestore.FirestoreProtoClient;

import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.localserver.service.Protos.Diff;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SubmitterTask implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  private static String DIFF_PATH = "/reviewer/data/diff";
  private static String CI_REQUESTS_PATH = "/reviewer/ci/requests";
  private static String CI_RESPONSES_HISTORY_PATH = "/reviewer/ci/responses/%d/history";

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private final ReentrantLock lock = new ReentrantLock();
  private final HashSet<CiRequest> ciSubmittedDiffIds = new HashSet<>();
  protected FirestoreProtoClient firestoreClient;

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

  @Override
  public void run() {
    if (lock.tryLock()) {
      try {
        // noinspection unchecked
        List<Diff> diffs = (List) firestoreClient.getProtoDocuments(DIFF_PATH, Diff.newBuilder());

        if (!fileUtils.folderExists("submitter")) {
          fileUtils.mkdirs("submitter");
        }

        diffs
            .stream()
            // get Diffs in SUBMITTING state
            .filter(diff -> diff.getStatus().equals(Diff.Status.SUBMITTING))
            // for each of them, get latest CiResponse
            .map(
                diff -> {
                  MessageWithId responseWithId =
                      firestoreClient.getDocumentFromCollection(
                          String.format(CI_RESPONSES_HISTORY_PATH, diff.getId()),
                          CiResponse.newBuilder());
                  if (responseWithId == null) {
                    return null;
                  }
                  CiResponse response = (CiResponse) responseWithId.message();
                  return Pair.of(diff, response);
                })
            // only proceed if CiResponse was present
            .filter(diffCiPair -> diffCiPair != null)
            // only proceed if TargetResult.success = true for all Results
            .filter(
                diffCiPair ->
                    diffCiPair
                        .getValue()
                        .getResultsList()
                        .stream()
                        .allMatch(x -> x.getStatus() == Status.SUCCESS))
            .forEach(
                diffCiPair -> {
                  Diff diff = diffCiPair.getKey();
                  CiResponse response = diffCiPair.getValue();

                  boolean shouldPush = true;
                  List<GitRepo> gitRepos = new ArrayList<>();

                  CiRequest.Builder newRequestBuilder =
                      CiRequest.newBuilder().setForSubmission(true);

                  for (CiResponse.TargetResult targetResult : response.getResultsList()) {
                    CiRequest.Target target = targetResult.getTarget();

                    Repo repo = target.getRepo();
                    String repoPath =
                        fileUtils.joinPaths(
                            fileUtils.getCurrentWorkingDirectory(), "submitter", repo.getId());
                    GitRepo gitRepo = gitRepoFactory.create(repoPath);
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
                    boolean allPushesSuccessful =
                        gitRepos.stream().allMatch(repo -> repo.push("master"));
                    if (allPushesSuccessful) {
                      diff = diff.toBuilder().setStatus(Diff.Status.SUBMITTED).build();
                      firestoreClient.setProtoDocument(
                          DIFF_PATH, String.valueOf(diff.getId()), diff);
                      // TODO: store `SubmitterMergeResult`
                    } else {
                      throw new RuntimeException("Not all pushes were successful");
                    }
                  } else {
                    CiRequest newRequest = newRequestBuilder.build();
                    if (!ciSubmittedDiffIds.contains(newRequest)) {
                      firestoreClient.setProtoDocument(
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

