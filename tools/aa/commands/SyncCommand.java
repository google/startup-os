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

package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.repo.GitRepoFactory;
import com.google.startupos.tools.aa.Protos.Config;
import java.io.IOException;
import javax.inject.Inject;

public class SyncCommand implements AaCommand {

  private FileUtils fileUtils;
  private Config config;

  private GitRepoFactory repoFactory;

  @Inject
  public SyncCommand(FileUtils utils, Config config, GitRepoFactory repoFactory) {
    this.fileUtils = utils;
    this.config = config;
    this.repoFactory = repoFactory;
  }

  @Override
  public void run(String[] args) {
    String headPath = fileUtils.joinPaths(this.config.getBasePath(), "head");

    try {
      fileUtils
          .listContents(headPath)
          .stream()
          .map(path -> fileUtils.joinPaths(headPath, path))
          .filter(path -> fileUtils.folderExists(path))
          .forEach(
              path -> {
                System.out.println("Performing sync: " + path.toString());
                repoFactory.create(path).pull();
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
