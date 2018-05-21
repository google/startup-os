package com.google.startupos.tools.aa.commands;

import com.google.common.collect.ImmutableList;
import com.google.startupos.tools.aa.commands.checks.DummyCheck;
import com.google.startupos.tools.aa.commands.checks.FailingDummyCheck;
import javax.inject.Inject;
import javax.inject.Named;

public class FixCommand implements AaCommand {

  public String currentWorkspaceName;

  public ImmutableList<FixCommandCheck> checks =
      ImmutableList.of(new DummyCheck(), new FailingDummyCheck());

  @Inject
  public FixCommand(@Named("Current workspace name") String currentWorkspaceName) {
    this.currentWorkspaceName = currentWorkspaceName;
  }

  @Override
  public void run(String[] args) {
    checks.forEach(
        check -> {
          System.out.println(String.format("check: [%s/%s]", currentWorkspaceName, check.name()));
          if (!check.perform()) {
            throw new RuntimeException(String.format("Check %s failed", check.name()));
          }
          ;
        });
  }
}
