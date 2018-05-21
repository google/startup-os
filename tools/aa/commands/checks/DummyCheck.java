package com.google.startupos.tools.aa.commands.checks;

import com.google.startupos.tools.aa.commands.FixCommandCheck;

public class DummyCheck implements FixCommandCheck {
  @Override
  public boolean perform() {
    System.err.println("performing a check: successful");
    return true;
  }

  @Override
  public String name() {
    return "dummy";
  }
}
