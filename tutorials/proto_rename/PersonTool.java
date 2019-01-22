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

package com.google.startupos.tutorials.proto_rename;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tutorials.proto_rename.Protos.Person;
import dagger.Component;

import javax.inject.Singleton;

public class PersonTool {
  private static String personProtobinaryPath;
  private static FileUtils fileUtils;

  public static void main(String[] args) {
    fileUtils = DaggerPersonTool_PersonToolComponent.create().getFileUtils();
    personProtobinaryPath =
        fileUtils.joinToAbsolutePath(fileUtils.getCurrentWorkingDirectory(), "person.protobin");

    if (!processArgs(args)) {
      System.exit(1);
    }

    if (args[0].equals("write")) {
      writePerson();
    } else {
      readPerson();
    }
  }

  private static void writePerson() {
    Person person =
        Person.newBuilder()
            .setName("John")
            .setId(1)
            .setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPLE)
            .build();

    fileUtils.writeProtoBinaryUnchecked(person, personProtobinaryPath);
  }

  private static void readPerson() {
    System.out.println(
        fileUtils.readProtoBinaryUnchecked(personProtobinaryPath, Person.newBuilder()));
  }

  private static boolean processArgs(String[] args) {
    if (args.length == 0) {
      System.out.println(
          "Missing input arg. Please use `read` arg for reading or `write` for writing.");
      return false;
    } else if (!args[0].equals("read") && !args[0].equals("write")) {
      System.out.println(
          "The input arg is incorrect. Please use `read` arg for reading or `write` for writing.");
      return false;
    }
    return true;
  }

  @Singleton
  @Component(modules = {CommonModule.class})
  interface PersonToolComponent {
    FileUtils getFileUtils();
  }
}

