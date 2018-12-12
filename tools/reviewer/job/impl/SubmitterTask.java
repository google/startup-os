package com.google.startupos.tools.reviewer.job.impl;

import com.google.common.flogger.FluentLogger;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreClientFactory;
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
import java.util.function.Predicate;

public class SubmitterTask extends FirestoreTaskBase implements Task {
    private static FluentLogger log = FluentLogger.forEnclosingClass();

    private FileUtils fileUtils;
    private GitRepoFactory gitRepoFactory;
    private final ReentrantLock lock = new ReentrantLock();
    private final HashSet<Long> ciSubmittedDiffIds = new HashSet<>();

    @Inject
    public SubmitterTask(
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
        /*
         * look for diffs with state = SUBMITTING
         *
         */
        if (lock.tryLock()) {
            try {
                initializeFirestoreClientIfNull();
                // noinspection unchecked
                List<Diff> diffs = (List) this.firestoreClient.listDocuments(
                        "/reviewer/data/diff", Diff.newBuilder());

                if (!fileUtils.folderExists("submitter")) {
                    fileUtils.mkdirs("submitter");
                }

                diffs.stream()
                        // get Diffs in SUBMITTING state
                    .filter(diff -> diff.getStatus().equals(Diff.Status.SUBMITTING))
                        // for each of them, get latest CIResponse (get or pop?)
                    .map(diff -> {
                        MessageWithId responseWithId = this.firestoreClient.popDocument(
                                String.format("/reviewer/ci/responses/%d/history", diff.getId()), CIResponse.newBuilder());
                        CIResponse response = (CIResponse) responseWithId.message();
                        return Pair.of(diff, response);
                    })
                        // only proceed if CIResponse was present
                    .filter(diffCiPair -> diffCiPair.getValue() != null)
                        // only proceed if TargetResult.success = true for all Results
                    .filter(diffCiPair -> diffCiPair.getValue().getResultsList().stream().allMatch(CIResponse.TargetResult::getSuccess))
                    .forEach(diffCiPair -> {
                            Diff diff = diffCiPair.getKey();
                            CIResponse response = diffCiPair.getValue();

                            boolean shouldPush = true;
                            List<GitRepo> gitRepos = new ArrayList<>();

                            for (CIResponse.TargetResult targetResult : response.getResultsList()) {
                                CIRequest.Target target = targetResult.getTarget();

                                Repo repo = target.getRepo();
                                String repoPath = fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "submitter", repo.getId());
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

                                gitRepo.switchBranch(String.format("origin/D%d", diff.getId()));
                                if (gitRepo.getHeadCommitId().equals(target.getCommitId())) {
                                    // latest result for CI matches last commit on the branch

                                    gitRepo.switchBranch("master");
                                    boolean mergingResult = gitRepo.merge("D234");
                                    if (!mergingResult || gitRepo.hasChanges("master")) {
                                        shouldPush = false;
                                    }

                                } else {
                                    // commit on the branch is newer than results in CI
                                    shouldPush = false;
                                    // prepare a CIRequest to later add it to Firestore CI reqs queue
                                    // CIRequest.newBuilder().setForSubmission(true).addTarget()
                                }
                            }

                            if (shouldPush) {
                                boolean allPushesSuccessful = gitRepos.stream().allMatch(repo -> repo.push("master"));
                                if (allPushesSuccessful) {
                                    // TODO: update Diff status to be Diff.Status.SUBMITTED
                                }
                            }
                    });

            } finally {
                lock.unlock();
            }
        }
    }
}
