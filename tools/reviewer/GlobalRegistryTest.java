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

package com.google.startupos.tools.reviewer;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import dagger.Component;
import dagger.Provides;
import java.nio.file.FileSystem;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import com.google.startupos.tools.reviewer.Protos.ReviewerRegistry;
import com.google.startupos.tools.reviewer.Protos.ReviewerRegistryConfig;

/* A test to check global_registry.prototxt is valid proto format */
public class GlobalRegistryTest {

  @Singleton
  @Component(modules = {CommonModule.class})
  interface TestComponent {
    FileUtils getFileUtils();
  }

  private FileUtils fileUtils;

  @Before
  public void setup() {
    TestComponent component = DaggerGlobalRegistryTest_TestComponent.create();
    fileUtils = component.getFileUtils();
  }

  // Test that global registry file can be read.
  @Test
  public void protoFileTest() throws Exception {
    fileUtils.readPrototxt(
        "tools/reviewer/global_registry.prototxt", ReviewerRegistry.newBuilder());
  }
}

