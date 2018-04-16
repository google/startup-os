package com.appstory.tools.aa.commands;

import com.appstory.tools.aa.Aa;
import com.appstory.tools.aa.Protos.Config;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import java.io.IOException;

public class InitCommand implements AaCommand {
  @FlagDesc(name = "base_path", description = "Base path", required = true)
  public static Flag<String> basePath = Flag.create("");

  @FlagDesc(name = "remote_repo_url", description = "Remote git repo", required = true)
  public static Flag<String> remoteRepoUrl = Flag.create("");

  @Override
  public void run() {
    try {
      Config config =
          Config.newBuilder()
              .setBasePath(basePath.get())
              .setRemoteRepoUrl(remoteRepoUrl.get())
              .build();
      FileUtils.writePrototxt(config, Aa.CONFIG_FILE);

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "init";
  }
}

