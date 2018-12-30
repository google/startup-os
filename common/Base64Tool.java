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

package com.google.startupos.common;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import dagger.Component;

import java.io.IOException;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Singleton;

/** A tool for converting a file to base64. This is useful for encoding secrets in Kubernetes. */
@Singleton
public class Base64Tool {
  @FlagDesc(name = "file", description = "File to output as base64", required = true)
  public static Flag<String> file = Flag.create("");

  @FlagDesc(name = "encode", description = "Encode")
  public static Flag<Boolean> encode = Flag.create(true);

  @FlagDesc(name = "decode", description = "Decode")
  public static Flag<Boolean> decode = Flag.create(false);

  private FileUtils fileUtils;

  @Inject
  Base64Tool(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  void run() throws IOException {
    String fileContents = fileUtils.readFile(file.get());
    if (encode.get()) {
      String encodedContents = Base64.getEncoder().encodeToString(fileContents.getBytes());
      System.out.print(encodedContents);
    }
    if (decode.get()) {
      String decodedContents = new String(Base64.getDecoder().decode(fileContents));
      System.out.print(decodedContents);
    }
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    Base64Tool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    DaggerBase64Tool_ToolComponent.create().getTool().run();
  }
}

