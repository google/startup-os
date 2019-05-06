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

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

/*
 * Usage without service_account_json flag:
 *   bazel run //tools/reviewer/job/tasks:reviewer_metadata_updater_task_tool
 * Usage with service_account_json flag:
 *   bazel run //tools/reviewer/job/tasks:reviewer_metadata_updater_task_tool -- --service_account_json <path/to/service/account_json>
 */
public class ReviewerMetadataUpdaterTaskTool {

  @FlagDesc(name = "service_account_json", description = "", required = false)
  public static Flag<String> serviceAccountJson = Flag.create("");

  public static void main(String[] args) {
    Flags.parseCurrentPackage(args);
    System.out.println(serviceAccountJson.get());
    FirestoreProtoClient client = new FirestoreProtoClient(serviceAccountJson.get());
    ReviewerMetadataUpdaterTask reviewerMetadataUpdaterTask = DaggerReviewerMetadataUpdaterTaskTool_ReviewerMetadataUpdaterTaskToolComponent
        .builder().setFirestoreProtoClient(client).build().getReviewerMetadataUpdaterTask();
    try {
      reviewerMetadataUpdaterTask.printStartupOsReviewerConfig();
    } catch (Exception e) {
      System.out.println(e);
    }
    try {
      reviewerMetadataUpdaterTask.printHasadnaReviewerConfig();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  @Singleton
  @Component(modules = { CommonModule.class })
  public interface ReviewerMetadataUpdaterTaskToolComponent {
    ReviewerMetadataUpdaterTask getReviewerMetadataUpdaterTask();

    @Component.Builder
    interface Builder {
      @BindsInstance
      ReviewerMetadataUpdaterTaskToolComponent.Builder setFirestoreProtoClient(
          FirestoreProtoClient firestoreProtoClient);

      ReviewerMetadataUpdaterTaskToolComponent build();
    }
  }
}