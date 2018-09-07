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

package com.google.startupos.proto_vs_json;

import com.google.protobuf.util.JsonFormat;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.proto_vs_json.Protos.Book;
import com.google.startupos.proto_vs_json.Protos.Person.PizzaTopping;
import com.google.startupos.proto_vs_json.Protos.Person;
import dagger.Component;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;

/* A simple tool to compare proto and json formats */
public class ProtoVsJsonTool {
  @FlagDesc(name = "prototxt_output", description = "Path to prototxt output file")
  private static final Flag<String> prototxtOutput = Flag.create("example.prototxt");

  @FlagDesc(name = "proto_binary_output", description = "Path to proto binary output file")
  private static final Flag<String> protoBinaryOutput = Flag.create("example.protobin");

  @FlagDesc(name = "json_output", description = "Path to json output file")
  private static final Flag<String> jsonOutput = Flag.create("example.json");

  private final FileUtils fileUtils;

  @Inject
  ProtoVsJsonTool(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  public void run() throws IOException {
    Person person =
        Person.newBuilder()
            .setName("John Smith")
            .setFavoriteBeatlesSong("Hey Jude")
            .setLuckyNumber(7)
            .addFavoritePizzaTopping(PizzaTopping.MUSHROOMS)
            .addFavoritePizzaTopping(PizzaTopping.BLACK_OLIVES)
            .addFavoritePizzaTopping(PizzaTopping.GREEN_PEPPERS)
            .addBookRead(
                Book.newBuilder().setName("To Kill a Mockingbird").setAuthor("Harper Lee").build())
            .addBookRead(
                Book.newBuilder().setName("Lord of the Flies").setAuthor("William Golding").build())
            .build();

    fileUtils.writeProtoBinary(person, protoBinaryOutput.get());
    fileUtils.writePrototxt(person, prototxtOutput.get());
    fileUtils.writeString(JsonFormat.printer().print(person), jsonOutput.get());
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  public interface MainComponent {
    ProtoVsJsonTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    DaggerProtoVsJsonTool_MainComponent.create().getTool().run();
  }
}

