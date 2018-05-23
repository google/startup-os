package com.google.startupos.tools.aa.commands.checks;

public class FailingDummyCheck implements FixCommandCheck {
  @Override
  public boolean perform() {
    System.err.println("performing a check: unsuccessful");
    return false;
  }

  @Override
  public String name() {
    return "dummy";
  }
}
