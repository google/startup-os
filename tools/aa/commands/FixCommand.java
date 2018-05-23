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
