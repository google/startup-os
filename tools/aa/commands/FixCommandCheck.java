package com.google.startupos.tools.aa.commands;

public interface FixCommandCheck {
  // returns whether the check identified
  // something wrong with the codebase
  boolean perform();

  String name();
}
