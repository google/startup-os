package com.appstory.tools.aa;

import com.appstory.tools.aa.commands.AaCommand;
import com.appstory.tools.aa.commands.InitCommand;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flags;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

/** aa logic. */
public class Aa {
  public static final String CONFIG_FILE = "~/.aaconfig.prototxt";

  private static HashMap<String, AaCommand> commands = new HashMap<>();
  private static Config config;

  static {
    AaCommand init = new InitCommand();
    commands.put(init.getName(), init);
  }

  public static void readConfig() throws IOException, ParseException {
    config = (Config) FileUtils.readPrototxt(CONFIG_FILE, Config.newBuilder());
  }

  public static void printUsage() {
    System.out.println(
        String.format(
            "Invalid usage; available commands are %s", String.join(", ", commands.keySet())));
    Flags.printUsage();
  }

  public static void main(String[] args) {
    String[] leftOverArgs =
        Flags.parse(
            args,
            Arrays.asList(
                Aa.class.getPackage().getName(), AaCommand.class.getPackage().toString()));

    if (leftOverArgs.length == 1) {
      String command = leftOverArgs[0];
      if (commands.containsKey(command)) {
        commands.get(command).run();
      } else {
        System.out.println("");
        printUsage();
      }
    } else {
      printUsage();
    }
  }
}

