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
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;

/** A command to init a base folder.
 *
 * Usage:
 * bazel run //tools/aa:aa_tool -- init --base_path </path/to/base/folder>
 * or, if aa is already set up:
 * aa init --base_path </path/to/base/folder>
*/
public class InitCommand implements AaCommand {
  public static final String BASE_FILENAME = "BASE";

  @FlagDesc(name = "base_path", description = "Base path", required = true)
  public static Flag<String> basePath = Flag.create("");

  @FlagDesc(name = "startupos_repo", description = "StartupOS git repo")
  public static Flag<String> startuposRepo = Flag.create("git@github.com:google/startup-os.git");

  @FlagDesc(name = "user", description = "User")
  public static Flag<String> user = Flag.create(System.getenv("USER"));

  private FileUtils fileUtils;

  @Inject
  public InitCommand(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  @Override
  public void run(String[] args) {
    // TODO: Add Flags.parse() support for specifying a particular class, not a whole package
    Flags.parse(args, InitCommand.class.getPackage());

    try {
      if (!fileUtils.folderEmptyOrNotExists(basePath.get())) {
        System.out.println("Error: Base folder exists and is not empty");
        System.exit(1);
      }
      // Create folders
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "head"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "ws"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "local"));
      fileUtils.mkdirs(fileUtils.joinPaths(basePath.get(), "logs"));

      // Write config
      Config config =
          Config.newBuilder()
              .setUser(user.get())
              .build();
      fileUtils.writePrototxt(
          config,
          fileUtils.joinPaths(basePath.get(), BASE_FILENAME));

      // Clone StartupOS repo into head:
      String startupOsPath = fileUtils.joinPaths(basePath.get(), "head", "startup-os");
      System.out.println("Cloning StartupOS into " + startupOsPath);
      Git.cloneRepository()
          .setURI(startuposRepo.get())
          .setDirectory(Paths.get(startupOsPath).toFile())
          .call();
      System.out.println("Completed Cloning");

    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      System.out.println("Input flags:");
      Flags.printUsage();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
