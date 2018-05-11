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
import com.google.startupos.tools.aa.Protos.Config;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class SyncCommand implements AaCommand {

  private FileUtils fileUtils;
  private Config config;

  @Inject
  public SyncCommand(FileUtils utils, Config config) {
    this.fileUtils = utils;
    this.config = config;
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
          .map(File::new)
          .forEach(
              pathAsFile -> {
                try {
                  System.out.println("Performing sync: " + pathAsFile.toString());
                  System.err.println(Git.open(pathAsFile).pull().call().toString());
                } catch (GitAPIException | IOException e) {
                  e.printStackTrace();
                }
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
