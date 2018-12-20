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

import com.google.startupos.tools.reviewer.job.impl.ReviewerMetadataUpdaterTask;
import com.google.startupos.tools.reviewer.job.impl.CiTask;
import com.google.startupos.tools.reviewer.job.impl.SubmitterTask;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor extends TimerTask {

  private List<Task> tasks;
  private ExecutorService threadPool;

  @Inject
  public TaskExecutor(
      ReviewerMetadataUpdaterTask reviewerMetadataUpdaterTask,
      CiTask ciTask,
      SubmitterTask submitterTask) {
    tasks = new ArrayList<>();
    tasks.add(reviewerMetadataUpdaterTask);
    tasks.add(ciTask);
    tasks.add(submitterTask);
    threadPool = Executors.newFixedThreadPool(4);
  }

  @Override
  public void run() {
    for (Task task : tasks) {
      if (task.shouldRun()) {
        threadPool.execute(task);
      }
    }
  }
}

