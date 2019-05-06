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

package com.google.startupos.tools.proto_tool;

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProtoTool {

  @FlagDesc(name = "proto_files", description = "Proto files, split by comma", required = true)
  public static final Flag<String> protoFiles = Flag.create("");

  @FlagDesc(name = "proto_txt", description = ".prototxt file", required = true)
  public static final Flag<String> protoTxt = Flag.create("");

  @FlagDesc(name = "proto_bin", description = ".protobin file (output)", required = true)
  public static final Flag<String> protoBin = Flag.create("");

  @FlagDesc(name = "proto_class", description = "FQN of proto class", required = true)
  public static final Flag<String> protoClass = Flag.create("");

  public static void main(String[] args) throws Exception {
    Flags.parseCurrentPackage(args);

    ProtoLoader.toProtobin(
        Arrays.stream(protoFiles.get().split(",")).map(Paths::get).collect(Collectors.toList()),
        Paths.get(protoTxt.get()),
        Paths.get(protoBin.get()),
        protoClass.get());
  }
}

