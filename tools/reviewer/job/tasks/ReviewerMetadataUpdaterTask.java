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
import com.google.startupos.tools.reviewer.ReviewerProtos.Contribution;
import com.google.startupos.tools.reviewer.ReviewerProtos.Project;
import com.google.startupos.tools.reviewer.ReviewerProtos.Repo;
import com.google.startupos.tools.reviewer.ReviewerProtos.ReviewerConfig;
import com.google.startupos.tools.reviewer.ReviewerProtos.SocialNetwork;
import com.google.startupos.tools.reviewer.ReviewerProtos.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
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
// TODO - How to make sure the first repo to be cloned is ALWAYS startup-os?
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
  private static final String REPO_REGISTRY_URL =
      "https://raw.githubusercontent.com/%s/%s/master/reviewer_config.prototxt";

  private final ReentrantLock lock = new ReentrantLock();

  private String registryChecksum;
  private Map<String, Integer> reviewerConfigHashes = new HashMap();
  private FileUtils fileUtils;
  private GitRepoFactory gitRepoFactory;
  protected FirestoreProtoClient firestoreClient;
  private boolean firstRun = true;

  @Inject
  public ReviewerMetadataUpdaterTask(
      FileUtils fileUtils, GitRepoFactory gitRepoFactory, FirestoreProtoClient firestoreClient) {
    this.fileUtils = fileUtils;
    this.gitRepoFactory = gitRepoFactory;
    this.firestoreClient = firestoreClient;
  }

  private void uploadReviewerRegistryToFirestore(ReviewerRegistry registry) {
    firestoreClient.setProtoDocument(
        REVIEWER_COLLECTION, REVIEWER_REGISTRY_DOCUMENT_NAME_BIN, registry);
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
        GitRepo repo = gitRepoFactory.create(REPO_DIRECTORY);

        if (fileUtils.folderEmptyOrNotExists(REPO_DIRECTORY)) {
          repo.cloneRepo(repoUrl.get(), REPO_DIRECTORY);
        } else {
          repo.pull();
        }

        String globalRegistryFilePath =
            fileUtils.joinToAbsolutePath(REPO_DIRECTORY, GLOBAL_REGISTRY_FILE);
        String newChecksum = md5ForFile(globalRegistryFilePath);
        ReviewerRegistry registry =
            (ReviewerRegistry)
                fileUtils.readPrototxtUnchecked(
                    globalRegistryFilePath, ReviewerRegistry.newBuilder());

        if (!newChecksum.equals(registryChecksum)) {
          if (firstRun) {
            log.atInfo().log("Storing on first run, checksum: %s", newChecksum);
          } else {
            log.atInfo().log(
                "New checksum not equal to stored one: new %s, stored %s",
                newChecksum, registryChecksum);
          }
          // uploadReviewerRegistryToFirestore(registry);
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

  public ReviewerConfig getReviewerConfig(String filePath) throws IOException {
    return (ReviewerConfig) fileUtils.readPrototxt(filePath, ReviewerConfig.newBuilder());
  }

  public String getStartupOsReviewerConfigPath() {
    return (String)
        fileUtils.joinPaths(fileUtils.getCurrentWorkingDirectory(), "reviewer_config.prototxt");
  }

  public String getHasadnaReviewerConfigPath() {
    return (String)
        fileUtils.joinPaths(fileUtils.expandHomeDirectory("~/hasadna"), "reviewer_config.prototxt");
  }

  private User getUser(ReviewerConfig reviewerConfig, String userId) {
    for (User user : reviewerConfig.getUserList()) {
      if (user.getId().equals(userId)) {
        return user;
      }
    }
    return User.getDefaultInstance();
  }

  public ReviewerConfig compareReviewerConfigData(
      ReviewerConfig reviewerConfig1, ReviewerConfig reviewerConfig2) {
    final String displayName = reviewerConfig1.getDisplayName();
    LinkedHashSet<Repo> repoList = new LinkedHashSet<>();
    repoList.addAll(reviewerConfig1.getRepoList());
    repoList.addAll(reviewerConfig2.getRepoList());
    LinkedHashSet<Project> projectList = new LinkedHashSet<>();
    projectList.addAll(reviewerConfig1.getProjectList());
    projectList.addAll(reviewerConfig2.getProjectList());
    LinkedHashSet<User> mergedUsersList = new LinkedHashSet<>();
    for (User user1 : reviewerConfig1.getUserList()) {
      User user2 = getUser(reviewerConfig2, user1.getId());
      if (user2.getId().isEmpty()) {
        // (user defined only in reviewerConfig1). Add `user1` to `mergedUsersList` without changes
        mergedUsersList.add(user1);
      } else {
        // (users defined in both repos). Merge data and add merged result to `mergedUsersList`
        String lastName = "";
        String email = "";
        String imageUrl = "";
        if (!user1.getLastName().isEmpty()) {
          lastName = user1.getLastName();
        }
        if (!user1.getEmail().isEmpty()) {
          if (!user1.getEmail().equals(user2.getEmail())) {
            System.out.println("***Emails for user " + user1.getId() + " differ between files.");
          }
          email = user1.getEmail();
        }
        if (!user1.getImageUrl().isEmpty()) {
          if (!user1.getImageUrl().equals(user2.getImageUrl())) {
            System.out.println(
                "***Image Urls for user " + user1.getId() + " differ between files.");
          }
          imageUrl = user1.getImageUrl();
        }
        if (user1.getCrystals() != user2.getCrystals()) {
          System.out.println(
              "***Crystals amount for user " + user1.getId() + " differ between files.");
        }
        int crystals = 0;
        crystals = user1.getCrystals();
        LinkedHashSet<SocialNetwork> mergedUserSocialNetworks = new LinkedHashSet<>();
        mergedUserSocialNetworks.addAll(user1.getSocialNetworkList());
        mergedUserSocialNetworks.addAll(user2.getSocialNetworkList());
        LinkedHashSet<String> mergedUserSkillList = new LinkedHashSet<>();
        mergedUserSkillList.addAll(user1.getSkillList());
        mergedUserSkillList.addAll(user2.getSkillList());
        LinkedHashSet<String> mergedUserProjectIdList = new LinkedHashSet<>();
        mergedUserProjectIdList.addAll(user1.getProjectIdList());
        mergedUserProjectIdList.addAll(user2.getProjectIdList());
        LinkedHashSet<Contribution> mergedUserContributions = new LinkedHashSet<>();
        mergedUserContributions.addAll(user1.getTopContributionList());
        mergedUserContributions.addAll(user2.getTopContributionList());
        User.Builder mergedUserBuilder =
            User.newBuilder()
                .setId(user1.getId())
                .setFirstName(user1.getFirstName())
                .setLastName(lastName)
                .setEmail(email)
                .setImageUrl(imageUrl)
                .setCrystals(crystals)
                .addAllSocialNetwork(mergedUserSocialNetworks)
                .addAllSkill(mergedUserSkillList)
                .addAllProjectId(mergedUserProjectIdList)
                .addAllTopContribution(mergedUserContributions);
        User mergedUser = mergedUserBuilder.build();
        mergedUsersList.add(mergedUser);
      }
    }
    List<User> usersDefinedOnlyInReviewerConfig2 =
        reviewerConfig2
            .getUserList()
            .stream()
            .filter(
                user2 -> {
                  boolean isUser2NotContainedInMergedUsers = true;
                  for (User mergedUser : mergedUsersList) {
                    if (mergedUser.getId().equals(user2.getId())) {
                      isUser2NotContainedInMergedUsers = false;
                      break;
                    }
                  }
                  return isUser2NotContainedInMergedUsers;
                })
            .collect(Collectors.toList());
    mergedUsersList.addAll(usersDefinedOnlyInReviewerConfig2);
    int totalCrystals = reviewerConfig1.getTotalCrystal();
    ReviewerConfig.Builder mergedReviewerConfig =
        ReviewerConfig.newBuilder()
            .setDisplayName(displayName)
            .addAllRepo(repoList)
            .addAllProject(projectList)
            .addAllUser(mergedUsersList)
            .setTotalCrystal(totalCrystals);
    return mergedReviewerConfig.build();
  }
}

