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

import com.google.startupos.common.FileUtils;
import com.google.startupos.tutorials.proto_rename.Protos.Person;

public class PersonTool {

  private static final String PERSON_PROTOBINARY_PATH;

  static {
    FileUtils fileUtils = DaggerProtoRenameComponent.create().getFileUtils();
    PERSON_PROTOBINARY_PATH = fileUtils.joinToAbsolutePath("person1.pb");
  }

  public static void main(String[] args) {
    if (!processArgs(args)) {
      System.exit(1);
    }

    if (args[0].equals("write")) {
      writePerson();
    } else {
      readPerson();
    }
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

  private static void readPerson() {
    System.out.println(new PersonReader().readPerson(PERSON_PROTOBINARY_PATH, Person.newBuilder()));
  }

  private static void writePerson() {
    Person person =
        Person.newBuilder()
            .setName("John Smith")
            .setId(1)
            .setFavoritePizzaTopping(Person.FavoritePizzaTopping.OLIVES_AND_PINEAPLE)
            .build();

    new PersonWriter().writePerson(PERSON_PROTOBINARY_PATH, person);
  }
}

