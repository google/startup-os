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

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.flogger.FluentLogger;
import com.google.startupos.tools.reviewer.job.ReviewerJob;
import com.google.startupos.tools.reviewer.job.tasks.Task;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.firestore.FirestoreClient;
import com.google.startupos.common.firestore.FirestoreClientFactory;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.RegistryProtos.ReviewerRegistry;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This task clones (or pulls) the repo, specified in `repo_url` command line argument Then it
 * checks whether `GLOBAL_REGISTRY_FILE` was changed as compared to previous version If it did,
 * newly updated version is posted to Firestore in proto and binary formats to
 * `REVIEWER_REGISTRY_DOCUMENT_NAME` and `REVIEWER_REGISTRY_DOCUMENT_NAME_BIN` documents in
 * `REVIEWER_REGISTRY_COLLECTION` collection
 */
public class ReviewerMetadataUpdaterTask implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  private static final String REVIEWER_REGISTRY_COLLECTION = "/reviewer";
  private static final String REVIEWER_REGISTRY_DOCUMENT_NAME = "registry";
  private static final String REVIEWER_REGISTRY_DOCUMENT_NAME_BIN = "registry_binary";

  private static final String REPO_DIRECTORY = "repo";
  private static final String GLOBAL_REGISTRY_FILE = "tools/reviewer/global_registry.prototxt";

  private final ReentrantLock lock = new ReentrantLock();

  private String storedChecksum;
  private FirestoreClient firestoreClient;

  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  private FirestoreClientFactory firestoreClientFactory;

  @Inject
  public ReviewerMetadataUpdaterTask(
      FileUtils fileUtils,
      GitRepoFactory gitRepoFactory,
      FirestoreClientFactory firestoreClientFactory) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
    this.firestoreClientFactory = firestoreClientFactory;
  }

  private void uploadProtoToFirestore(ReviewerRegistry registry) {
    firestoreClient.createDocument(
        REVIEWER_REGISTRY_COLLECTION, REVIEWER_REGISTRY_DOCUMENT_NAME, registry);
    firestoreClient.createProtoDocument(
        REVIEWER_REGISTRY_COLLECTION, REVIEWER_REGISTRY_DOCUMENT_NAME_BIN, registry);
  }

  private void initializeFirestoreClientIfNull() {
    if (this.firestoreClient == null) {
      FileInputStream serviceAccount = null;
      try {
        serviceAccount = new FileInputStream(ReviewerJob.serviceAccountJson.get());
        ServiceAccountCredentials cred =
            (ServiceAccountCredentials)
                GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(
                        Arrays.asList(
                            "https://www.googleapis.com/auth/cloud-platform",
                            "https://www.googleapis.com/auth/datastore"));
        this.firestoreClient =
            this.firestoreClientFactory.create(
                cred.getProjectId(), cred.refreshAccessToken().getTokenValue());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static Stream<Integer> intStream(byte[] array) {
    return IntStream.range(0, array.length).map(idx -> array[idx]).boxed();
  }

  private static String md5ForFile(String filePath) throws IOException {
    byte[] fileAsBytes = Files.readAllBytes(Paths.get(filePath));
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(fileAsBytes);
      byte[] messageDigestBytes = messageDigest.digest();
      StringBuilder hashBuilder = new StringBuilder();
      intStream(messageDigestBytes)
          .map(digestByte -> String.format("%02X", Byte.toUnsignedInt(digestByte.byteValue())))
          .forEach(hashBuilder::append);
      return hashBuilder.toString();
    } catch (NoSuchAlgorithmException ex) {
      // fallback to full file contents
      // this should not happen, MD5 is widely available
      return new String(fileAsBytes);
    }
  }

  @Override
  public void run() {
    if (lock.tryLock()) {
      try {
        initializeFirestoreClientIfNull();

        GitRepo repo = gitRepoFactory.create(REPO_DIRECTORY);

        if (fileUtils.folderEmptyOrNotExists(REPO_DIRECTORY)) {
          repo.cloneRepo(ReviewerJob.repoUrl.get(), REPO_DIRECTORY);
        } else {
          repo.pull();
        }

        String globalRegistryFilePath =
            fileUtils.joinToAbsolutePath(REPO_DIRECTORY, GLOBAL_REGISTRY_FILE);
        String newChecksum = md5ForFile(globalRegistryFilePath);

        if (!newChecksum.equals(storedChecksum)) {
          log.atInfo()
              .log(
                  "New checksum not equal to stored one: new %s, stored %s",
                  newChecksum, storedChecksum);

          ReviewerRegistry reg =
              (ReviewerRegistry)
                  fileUtils.readPrototxtUnchecked(
                      globalRegistryFilePath, ReviewerRegistry.newBuilder());

          uploadProtoToFirestore(reg);

          storedChecksum = newChecksum;

        } else {
          log.atInfo().log("Checksum equals to stored: found %s,", storedChecksum);
        }

      } catch (IOException exception) {
        exception.printStackTrace();
      } finally {
        lock.unlock();
      }
    }
  }

  @Override
  public Boolean shouldRun() {
    return !lock.isLocked();
  }
}

