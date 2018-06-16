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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.stream.Collectors;


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
   * @param packages list of package roots to scan for flags
   */
  public static String[] parse(String[] args, Package... packages) {
    String[] packageNames =
        Arrays.stream(packages).map(Package::getName).collect(Collectors.toList())
            .toArray((new String[0]));
    return parse(args, packageNames);
  }

  /**
   * Initializes flag values from command-line style arguments.
   *
   * @param args command-line arguments to parse values from
   * @param packages list of package roots to scan flags
   */
  public static String[] parse(String[] args, String... packages) {
    instance().scanPackages(Arrays.asList(packages));
    return _parse(args);
  }

  /**
   * Initializes flag values from command-line style arguments.
   *
   * @param args command-line arguments to parse values from
   * @param clazz class to scan flags
   */
  public static String[] parse(String[] args, Class clazz) {
    instance().scanClass(clazz);
    return _parse(args);
  }

  /** Prints user-readable usage help for all flags in a given package */
  public static void printUsage() {
    UsagePrinter.printUsage(getFlags(), System.out);
  }

  @VisibleForTesting
  public static Map<String, FlagData> getFlags() {
    return instance().flags;
  }

  @VisibleForTesting
  static String getFlagValue(String name) {
    FlagData flagData = getFlag(name);
    if (flagData == null) {
      throw new IllegalArgumentException(
          String.format(
              // TODO flagData is always null. Think of a more informative message
              "Flag data '%s' is null; did you forget to call FlagData.parse()?", flagData));
    }
    if (!flagData.getHasValue()) {
      return null;
    }
    return flagData.getValue();
  }

  @VisibleForTesting
  static FlagData getFlag(String name) {
    return getFlags().get(name);
  }

  @VisibleForTesting
  static void setFlagValue(String name, String value) {
    FlagData.Builder builder = getFlag(name).toBuilder();
    if (builder.getIsListFlag()){
      builder.setValue(value
          .replace("[", "")
          .replace("]", "")
          .replaceAll(", ", ","));
    } else {
      builder.setValue(value);
    }
    getFlags().put(name, builder.setHasValue(true).build());
  }

  @VisibleForTesting
  static String getDefaultFlagValue(String flagName) {
    return getFlag(flagName).getDefault();
  }

  private void scanPackages(Iterable<String> packages) {
    for (String packageName : packages) {
      try {
        classScanner.scanPackage(packageName, getFlags());
      } catch (IOException e) {
        throw new RuntimeException("Package cannot be scanned: " + packageName, e);
      }
    }
  }

  private void scanClass(Class clazz) {
    classScanner.scanClass(clazz, getFlags());
  }

  private static Flags instance() {
    synchronized (Flags.class) {
      if (instance == null) {
        instance = new Flags(new ClassScanner(), new HashMap<>());
      }
    }
    return instance;
  }

  public static void resetForTesting() {
    instance = new Flags(new ClassScanner(), new HashMap<>());
  }

  private static String[] _parse(String[] args) {
    GflagsParser parser = new GflagsParser(getFlags());
    return parser.parse(args).unusedArgs.toArray(new String[0]);
  }

  private Flags(ClassScanner classScanner, Map<String, FlagData> flags) {
    this.classScanner = classScanner;
    this.flags = flags;
  }
}
