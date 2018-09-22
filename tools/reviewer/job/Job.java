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

package com.google.startupos.tools.reviewer.tools.reviewer.job;

import com.google.startupos.tools.reviewer.tools.reviewer.job.tasks.TaskExecutor;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Timer;

@Singleton
public class Job {
  private TaskExecutor taskExecutor;

  @FlagDesc(name = "service_account_json", description = "", required = true)
  public static Flag<String> serviceAccountJson = Flag.create("");

  @FlagDesc(name = "repo_url", description = "", required = false)
  public static Flag<String> repoUrl = Flag.create("");

  @Inject
  public Job(TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  public interface JobComponent {
    Job getJob();
  }

  private void run(String[] args) {
    Flags.parseCurrentPackage(args);

    new Timer().scheduleAtFixedRate(this.taskExecutor, 0, 1000L);
  }

  public static void main(String[] args) {
    DaggerJob_JobComponent.create().getJob().run(args);
  }
}

