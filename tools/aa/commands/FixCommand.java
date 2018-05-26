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

package com.google.startupos.tools.aa.commands;

import com.google.common.collect.ImmutableList;
import com.google.startupos.tools.aa.commands.checks.DummyCheck;
import com.google.startupos.tools.aa.commands.checks.FailingDummyCheck;
import com.google.startupos.tools.aa.commands.checks.FixCommandCheck;
import javax.inject.Inject;

public class FixCommand implements AaCommand {

  public ImmutableList<FixCommandCheck> checks =
      ImmutableList.of(new DummyCheck(), new FailingDummyCheck());

  @Inject
  public FixCommand() {}

  @Override
  public void run(String[] args) {
    checks.forEach(
        check -> {
          System.out.println(String.format("check: %s", check.name()));
          if (!check.perform()) {
            throw new RuntimeException(String.format("Check %s failed", check.name()));
          }
        });
  }
}
