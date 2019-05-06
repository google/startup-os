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
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.repo.GitRepo;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.reviewer.RegistryProtos.ReviewerRegistry;
import com.google.startupos.tools.reviewer.ReviewerProtos.ReviewerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;

/*
 * This task periodically updates the reviewer configs in Firestore.
 *
 * It does it by:
 * 1. Cloning (and then pulling) the repo, specified by the `repo_url` flag.
 * 2. Checking whether `GLOBAL_REGISTRY_FILE` was changed as compared to previous version.
 * 3. If it did, a newly updated version is posted to Firestore in proto and binary formats.
 * 4. Doing the same for the config of every Reviewer in the registry.
 */
public class ReviewerMetadataUpdaterTask implements Task {
  private static FluentLogger log = FluentLogger.forEnclosingClass();

  @FlagDesc(name = "repo_url", description = "", required = false)
  public static Flag<String> repoUrl = Flag.create("");

  private static final String REVIEWER_COLLECTION = "/reviewer";
  private static final String REVIEWER_REGISTRY_DOCUMENT_NAME = "registry";
  private static final String REVIEWER_REGISTRY_DOCUMENT_NAME_BIN = "registry_binary";
  private static final String REVIEWER_CONFIG_DOCUMENT_NAME = "config";
  private static final String REVIEWER_CONFIG_DOCUMENT_NAME_BIN = "config_binary";

  private static final String REPO_DIRECTORY = "repo";
  private static final String GLOBAL_REGISTRY_FILE = "tools/reviewer/global_registry.prototxt";
  private static final String REPO_REGISTRY_FILE = "reviewer_config.prototxt";
  private static final String REPO_REGISTRY_URL = "https://raw.githubusercontent.com/%s/%s/master/reviewer_config.prototxt";

  private final ReentrantLock lock = new ReentrantLock();

  private String registryChecksum;
  private Map<String, Integer> reviewerConfigHashes = new HashMap();
  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  protected FirestoreProtoClient firestoreClient;
  private boolean firstRun = true;

  @Inject
  public ReviewerMetadataUpdaterTask(FileUtils fileUtils, GitRepoFactory gitRepoFactory,
      FirestoreProtoClient firestoreClient) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
    this.firestoreClient = firestoreClient;
  }

  private void uploadReviewerRegistryToFirestore(ReviewerRegistry registry) {
    firestoreClient.setProtoDocument(REVIEWER_COLLECTION, REVIEWER_REGISTRY_DOCUMENT_NAME_BIN, registry);
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
      intStream(messageDigestBytes).map(digestByte -> String.format("%02X", Byte.toUnsignedInt(digestByte.byteValue())))
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
        GitRepo repo = gitRepoFactory.create(REPO_DIRECTORY);

        if (fileUtils.folderEmptyOrNotExists(REPO_DIRECTORY)) {
          repo.cloneRepo(repoUrl.get(), REPO_DIRECTORY);
        } else {
          repo.pull();
        }

        String globalRegistryFilePath = fileUtils.joinToAbsolutePath(REPO_DIRECTORY, GLOBAL_REGISTRY_FILE);
        String newChecksum = md5ForFile(globalRegistryFilePath);
        ReviewerRegistry registry = (ReviewerRegistry) fileUtils.readPrototxtUnchecked(globalRegistryFilePath,
            ReviewerRegistry.newBuilder());

        if (!newChecksum.equals(registryChecksum)) {
          if (firstRun) {
            log.atInfo().log("Storing on first run, checksum: %s", newChecksum);
          } else {
            log.atInfo().log("New checksum not equal to stored one: new %s, stored %s", newChecksum, registryChecksum);
          }
          //uploadReviewerRegistryToFirestore(registry);
          registryChecksum = newChecksum;
        } else {
          log.atInfo().log("Checksum equals to stored one: %s,", registryChecksum);
        }
        firstRun = false;
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

  public void printStartupOsReviewerConfig() throws IOException {
    String localRegistryFilePath = fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(),
        "reviewer_config.prototxt");
    ReviewerConfig reviewerConfig = (ReviewerConfig) fileUtils.readPrototxt(localRegistryFilePath,
        ReviewerConfig.newBuilder());
    System.out.println("StartupOs ReviewerConfig:\n"  + reviewerConfig.toString());
  }

  public void printHasadnaReviewerConfig() throws IOException {
    String targetDirectory = fileUtils.expandHomeDirectory("~/hasadna");
    String localHasadnaRegistryFilePath = fileUtils.joinPaths(targetDirectory,
      "reviewer_config.prototxt");
    ReviewerConfig reviewerConfigHasadna = (ReviewerConfig) fileUtils.readPrototxt(localHasadnaRegistryFilePath,
        ReviewerConfig.newBuilder());
    System.out.println("Hasadna ReviewerConfig:\n" + reviewerConfigHasadna.toString());
  }
}
