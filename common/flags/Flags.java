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

package com.google.startupos.common.flags;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static API for creating and reading flags.
 *
 * <p>For examples, see test classes (e.g FlagDescTestClass).
 *
 * <p>Some notes: - No two flags can have the same flag name. - Flag variable names are camelCase. -
 * Flag names are underscore_case. - If flags are accessed before parsing is completed, they may
 * return default values instead of parsed ones. Once parsed, flags will return the parsed values.
 * To avoid this, parsing should be done before any multi-threaded work. - All checks are enforced
 * in runtime, not compile time.
 *
 * <p>Implementation note: Most data is stored in FlagData proto and accessed by flag. ClassScanner
 * populates FlagData and then GflagsParser inserts args values into it.
 */
public class Flags {
  private static Flags instance;
  private final ClassScanner classScanner;
  private final Map<String, FlagData> flags;

  /**
   * Initializes flag values from command-line style arguments.
   *
   * @param args command-line arguments to parse values from
   * @param packages list of package roots to scan flags
   */
  public static String[] parse(String[] args, Iterable<String> packages) {
    instance().scan(packages);
    return instance._parse(args);
  }

  /** Prints user-readable usage help for all flags in a given package */
  public static void printUsage() {
    new UsagePrinter().printUsage(instance().getFlags(), System.out);
  }

  @VisibleForTesting
  public static Map<String, FlagData> getFlags() {
    return instance().flags;
  }

  @VisibleForTesting
  static String getFlagValue(String name) {
    if (!instance().getFlag(name).getHasValue()) {
      return null;
    }
    return instance().getFlag(name).getValue();
  }

  @VisibleForTesting
  static FlagData getFlag(String name) {
    return instance().flags.get(name);
  }

  @VisibleForTesting
  static void setFlagValue(String name, String value) {
    instance().flags.put(name, getFlag(name).toBuilder().setValue(value).setHasValue(true).build());
  }

  @VisibleForTesting
  static String getDefaultFlagValue(String flagName) {
    return instance().getFlag(flagName).getDefault();
  }

  private void scan(Iterable<String> packages) {
    for (String pkg : packages) {
      classScanner.scanPackage(pkg, flags);
    }
  }

  private static Flags instance() {
    synchronized (Flags.class) {
      if (instance == null) {
        instance = new Flags(new ClassScanner(), new HashMap<String, FlagData>());
      }
    }
    return instance;
  }

  private Flags(ClassScanner classScanner, Map<String, FlagData> flags) {
    this.classScanner = classScanner;
    this.flags = flags;
  }

  private String[] _parse(String[] args) {
    GflagsParser parser = new GflagsParser(flags);
    return parser.parse(args).unusedArgs.toArray(new String[0]);
  }
}

