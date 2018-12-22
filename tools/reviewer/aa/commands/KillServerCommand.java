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

package com.google.startupos.tools.reviewer.aa.commands;

import javax.inject.Inject;
import com.google.startupos.common.FileUtils;

public class KillServerCommand implements AaCommand {

  @Inject
  public KillServerCommand() {}

  private Integer getPid(int port) throws Exception {
    // -t makes `lsof` output only PIDs so output can be piped to `kill`
    // -n and -P prevent `lsof` from resolving addresses and ports, therefore
    // making execution faster
    // We filter for processes that listen on port in order to kill only
    // servers (i.e. Angular) instead of clients (i.e. Chrome)
    String[] command = new String[] {"lsof", "-tnP", "-i:" + port, "-sTCP:LISTEN"};
    Process process = Runtime.getRuntime().exec(command);
    String output = FileUtils.streamToString(process.getInputStream()).trim();
    if (output.isEmpty()) {
      return null;
    }
    return Integer.parseInt(output);
  }

  private void killServer(int port) throws Exception {
    Integer pid = getPid(port);
    if (pid != null) {
      Runtime.getRuntime().exec(new String[] {"kill", "-9", pid + ""});
    }
  }

  @Override
  public boolean run(String[] args) {
    if (args.length != 1) {
      System.err.println("Incorrect args. Use: 'killserver'");
    }
    try {
      killServer(8001);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return true;
  }
}

