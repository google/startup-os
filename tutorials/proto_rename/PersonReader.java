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

import javax.inject.Inject;

public class PersonReader {
  private FileUtils fileUtils;

  @Inject
  PersonReader() {
    fileUtils = DaggerProtoRenameComponent.create().getFileUtils();
  }

  Person readPerson(String path, Person.Builder builder) {
    return (Person) fileUtils.readProtoBinaryUnchecked(path, builder);
  }
}

