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
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.aa.Protos.Config;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class AddRepoCommand implements AaCommand {

  private final FileUtils fileUtils;
  private final Config config;

  @Inject
  public AddRepoCommand(FileUtils utils, Config config) {
    this.fileUtils = utils;
    this.config = config;
  }

  @FlagDesc(name = "url", description = "Repository URL to add", required = true)
  public static Flag<String> url = Flag.create("");

  @FlagDesc(name = "name", description = "Repository name")
  public static Flag<String> name = Flag.create("");

  private String getNameFromRemoteUrl(String remoteUrl) throws URISyntaxException {
    String path = new URI(remoteUrl).getPath();
    return path.substring(path.lastIndexOf("/") + 1).replace(".git", "");
  }

  @Override
  public boolean run(String[] args) {
    Flags.parse(args, AddRepoCommand.class.getPackage());

    String headPath = fileUtils.joinPaths(this.config.getBasePath(), "head");
    String repoName = name.get();

    if (repoName.isEmpty()) {
      try {
        repoName = getNameFromRemoteUrl(url.get());
      } catch (URISyntaxException e) {
        System.err.println(RED_ERROR + "Could not parse repository URL");
        System.err.println(
            YELLOW_NOTE + "If you are sure it is correct, specify directory name with --name");
        System.err.println(YELLOW_NOTE + "This way, add_repo will not try to guess it from URL");
        e.printStackTrace();
        return false;
      }
    }

    String repoPath = fileUtils.joinPaths(headPath, repoName);
    System.out.println(String.format("Cloning repo %s into %s", repoName, repoPath));
    try {
      Git.cloneRepository().setURI(url.get()).setDirectory(Paths.get(repoPath).toFile()).call();
    } catch (GitAPIException e) {
      System.err.println(RED_ERROR + "Could not clone repository");
      e.printStackTrace();
      return false;
    }

    System.err.println("Completed cloning");
    return true;
  }
}

