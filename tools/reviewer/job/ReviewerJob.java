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

package com.google.startupos.tools.reviewer.job;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.firestore.FirestoreProtoClient;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.local_server.LocalHttpGateway;
import com.google.startupos.tools.local_server.LocalServer;
import com.google.startupos.tools.local_server.LocalServer.HeadUpdater;
import com.google.startupos.tools.reviewer.aa.AaModule;
import com.google.startupos.tools.reviewer.aa.commands.InitCommand;
import com.google.startupos.tools.reviewer.job.tasks.CiTask;
import com.google.startupos.tools.reviewer.job.tasks.ReviewerMetadataUpdaterTask;
import com.google.startupos.tools.reviewer.job.tasks.SubmitterTask;
import com.google.startupos.tools.reviewer.job.tasks.TaskExecutor;
import com.google.startupos.tools.reviewer.local_server.service.CodeReviewService;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Lazy;
import java.util.Timer;
import javax.inject.Inject;
import javax.inject.Singleton;

// To run on server:
// bazel run //tools/reviewer/job -- \
// --service_account_json
// /home/startup_os_project/base/local/startupos-5f279-firebase-adminsdk-v8n9e-2418a5ae73.json \
// --repo_url https://github.com/google/startup-os &
// disown -a

/* TODO:
 * Would be nice to be able to have every task run at different periodicity, and choose whether it:
 *
 * Wants overlapping runs
 * Wants to skip overlapping runs
 * Wants to run immediately after overlapping run ends
 * Wants to run some time after previous run ended
 * We don't have to implement that now, but those are the main scenarios I think we'll encounter.
 * It would be nice it the task would choose the strategy and parameters,
 * and some common code would take care of it.
 */
@Singleton
public class ReviewerJob {
  private static final Long TASK_EXECUTION_PERIOD_MS = 5 * 60 * 1000L;

  @FlagDesc(name = "service_account_json", description = "", required = true)
  public static Flag<String> serviceAccountJson = Flag.create("");

  private TaskExecutor taskExecutor;
  private InitCommand initCommand;
  // These are lazy because constructing them requires us to be in a base folder.
  // That base folder is only set up in run() using initCommand. We also assumme we're inside it.
  private Lazy<LocalServer> lazyLocalServer;
  private Lazy<HeadUpdater> lazyHeadUpdater;

  @Inject
  public ReviewerJob(
      InitCommand initCommand,
      Lazy<LocalServer> lazyLocalServer,
      Lazy<HeadUpdater> lazyHeadUpdater,
      ReviewerMetadataUpdaterTask reviewerMetadataUpdaterTask,
      CiTask ciTask,
      SubmitterTask submitterTask) {
    this.taskExecutor =
        new TaskExecutor(ImmutableList.of(reviewerMetadataUpdaterTask, ciTask, submitterTask));
    this.initCommand = initCommand;
    this.lazyLocalServer = lazyLocalServer;
    this.lazyHeadUpdater = lazyHeadUpdater;
  }

  @Singleton
  @Component(modules = {AaModule.class, CommonModule.class})
  public interface JobComponent {
    ReviewerJob getJob();

    @Component.Builder
    interface Builder {
      @BindsInstance
      Builder setFirestoreProtoClient(FirestoreProtoClient firestoreProtoClient);

      JobComponent build();
    }
  }

  private void run() throws Exception {
    initCommand.run(InitCommand.basePath.get(), InitCommand.startuposRepo.get());
    new Timer()
        .scheduleAtFixedRate(lazyHeadUpdater.get(), 0, LocalServer.pullFrequency.get() * 1000L);
    // delay = 0, meaning timer starts right away
    // TASK_EXECUTION_PERIOD_MS denotes period at which task is being queued
    new Timer().scheduleAtFixedRate(this.taskExecutor, 0, TASK_EXECUTION_PERIOD_MS);
    lazyLocalServer.get().start();
    new LocalHttpGateway(
            LocalServer.httpGatewayPort.get(),
            LocalServer.localServerHost.get(),
            LocalServer.localServerPort.get())
        .serve();
    lazyLocalServer.get().blockUntilShutdown();
  }

  public static void main(String[] args) throws Exception {
    Flags.parse(
        args,
        ReviewerJob.class.getPackage(),
        ReviewerMetadataUpdaterTask.class.getPackage(),
        InitCommand.class.getPackage(),
        LocalServer.class.getPackage(),
        CodeReviewService.class.getPackage());
    FirestoreProtoClient client = new FirestoreProtoClient(serviceAccountJson.get());
    DaggerReviewerJob_JobComponent.builder().setFirestoreProtoClient(client).build().getJob().run();
  }
}

