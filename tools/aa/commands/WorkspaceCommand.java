package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.tools.aa.Protos.Config;

import java.io.IOException;
import java.nio.file.Paths;
import javax.inject.Inject;

public class WorkspaceCommand implements AaCommand {
  @FlagDesc(
    name = "force",
    description = "Force workspace switching creating workspace if non-existent"
  )
  public static Flag<Boolean> force = Flag.create(false);

  @FlagDesc(name = "ws", description = "Workspace name to switch to", required = true)
  public static Flag<String> ws = Flag.create("");

  private FileUtils fileUtils;
  private Config config;

  @Inject
  public WorkspaceCommand(FileUtils utils, Config config) {
    this.fileUtils = utils;
    this.config = config;
  }

  @Override
  public void run(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-f")) {
        args[i] = "--force";
      }
    }
    Flags.parse(args, WorkspaceCommand.class.getPackage());
    String basePath = config.getBasePath();

    String user = System.getenv("USER");
    String headPath = Paths.get(basePath, "head").toAbsolutePath().toString();
    String newWsPath = Paths.get(basePath, user, "ws", ws.get()).toAbsolutePath().toString();

    if (force.get()) {
      fileUtils.mkdirs(newWsPath);
      try {
        fileUtils.copyDirectoryToDirectory(headPath, newWsPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    System.out.println(String.format("cd %s", newWsPath));
  }

  @Override
  public String getName() {
    return "workspace";
  }
}
