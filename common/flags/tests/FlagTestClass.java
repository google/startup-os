package com.google.startupos.common.flags.testpackage1;

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;

public class FlagTestClass {
  @SuppressWarnings("unused")
  @FlagDesc(name = "string_test_flag", description = "A flag description")
  public static final Flag<String> stringTestFlag = Flag.create("default value");

  public class ClassWithoutFlags {
  }
}
