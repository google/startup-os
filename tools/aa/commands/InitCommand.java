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
import com.google.startupos.tools.aa.Protos.RemoteRepo;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;

public class InitCommand implements AaCommand {
  @FlagDesc(name = "config_path", description = "Path to config file", required = true)
  public static Flag<String> configPath = Flag.create("");

  @FlagDesc(name = "base_path", description = "Base path", required = true)
  public static Flag<String> basePath = Flag.create("");

  @FlagDesc(name = "remote_repo_url", description = "Remote git repo", required = true)
  public static Flag<String> remoteRepoUrl = Flag.create("");

  @FlagDesc(name = "user", description = "User")
  public static Flag<String> user = Flag.create(System.getenv("USER"));

  private FileUtils fileUtils;

  @Inject
  public InitCommand(FileUtils utils) {
    this.fileUtils = utils;
  }

  private String getNameFromRemoteUrl(String remoteUrl) throws URISyntaxException {
    String path = new URI(remoteRepoUrl.get()).getPath();
    return path.substring(path.lastIndexOf("/") + 1).replace(".git", "");
  }

  @Override
  public void run(String[] args) {
    Flags.parse(args, InitCommand.class.getPackage());

    try {
      String remoteRepoId = getNameFromRemoteUrl(remoteRepoUrl.get());
      Config config =
          Config.newBuilder()
              .setBasePath(basePath.get())
              .setUser(user.get())
              .addRemoteRepo(
                  RemoteRepo.newBuilder().setId(remoteRepoId).setUrl(remoteRepoUrl.get()))
              .build();
      fileUtils.writePrototxt(config, configPath.get());

    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Input flags:");
      Flags.printUsage();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "init";
  }
}
