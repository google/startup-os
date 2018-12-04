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

import com.google.startupos.tools.reviewer.job.tasks.TaskExecutor;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Timer;

@Singleton
public class ReviewerJob {
  private static final Long TASK_EXECUTION_PERIOD_MS = 5 * 60 * 1000L;
  private TaskExecutor taskExecutor;

  @FlagDesc(name = "service_account_json", description = "", required = true)
  public static Flag<String> serviceAccountJson = Flag.create("");

  @FlagDesc(name = "repo_url", description = "", required = false)
  public static Flag<String> repoUrl = Flag.create("");

  @Inject
  public ReviewerJob(TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  public interface JobComponent {
    ReviewerJob getJob();
  }

  private void run(String[] args) {
    Flags.parseCurrentPackage(args);

    // delay = 0, meaning timer starts right away
    // TASK_EXECUTION_PERIOD_MS denotes period at which task is being queued

    /* TODO:
     * Would be nice to be able to have every task run at different periodicity, and choose whether it:
     *
     * Wants overlapping runs
     * Wants to skip overlapping runs
     * Wants to run immediately after overlapping run ends
     * Wants to run some time after previous run ended
     * We don't have to implement that now, but those are the main scenarios I think we'll encounter.
     * It would be nice it the task would choose the strategy and parameters, and some common code would take care of it.
     */
    new Timer().scheduleAtFixedRate(this.taskExecutor, 0, TASK_EXECUTION_PERIOD_MS);
  }

  public static void main(String[] args) {
    DaggerReviewerJob_JobComponent.create().getJob().run(args);
  }
}

