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

package com.google.startupos.common.firestore.tests;

import com.google.startupos.common.CommonModule;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.startupos.common.FileUtils;
import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import com.google.startupos.common.flags.Flags;
import com.google.startupos.common.repo.GitRepo;
import java.io.IOException;
import com.google.startupos.common.firestore.FirestoreAdminClient;

/** A tool for testing TextDifferencer. */
@Singleton
public class FirestoreAdminClientTool {
  @FlagDesc(name = "service_account_json", description = "", required = true)
  public static Flag<String> serviceAccountJson = Flag.create("");

  private FileUtils fileUtils;

  @Inject
  FirestoreAdminClientTool(FileUtils fileUtils) {
    this.fileUtils = fileUtils;
  }

  void run() throws IOException {
    FirestoreAdminClient firestoreAdminClient = new FirestoreAdminClient(serviceAccountJson.get());
    firestoreAdminClient.testFunctionality();
  }

  @Singleton
  @Component(modules = CommonModule.class)
  interface ToolComponent {
    FirestoreAdminClientTool getTool();
  }

  public static void main(String[] args) throws IOException {
    Flags.parseCurrentPackage(args);
    DaggerFirestoreAdminClientTool_ToolComponent.create().getTool().run();
  }
}

