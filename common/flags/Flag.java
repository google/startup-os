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

import javax.annotation.Nonnull;
import com.google.common.flogger.FluentLogger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Flag class, with implementations for various flag types. */
public abstract class Flag<T> {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  public static Flag<String> create(String defaultValue) {
    return new Flag.StringFlag(defaultValue);
  }

  public static Flag<Boolean> create(Boolean defaultValue) {
    return new Flag.BooleanFlag(defaultValue);
  }

  public static Flag<Integer> create(Integer defaultValue) {
    return new Flag.IntegerFlag(defaultValue);
  }

  public static Flag<Long> create(Long defaultValue) {
    return new Flag.LongFlag(defaultValue);
  }

  public static Flag<Double> create(Double defaultValue) {
    return new Flag.DoubleFlag(defaultValue);
  }

  public static Flag<List<String>> createStringsListFlag(List<String> defaultValue) {
    return new Flag.StringsListFlag(defaultValue);
  }

  public static Flag<List<Boolean>> createBooleansListFlag(List<Boolean> defaultValue) {
    return new Flag.BooleansListFlag(defaultValue);
  }

  public static Flag<List<Integer>> createIntegersListFlag(List<Integer> defaultValue) {
    return new Flag.IntegersListFlag(defaultValue);
  }

  public static Flag<List<Long>> createLongsListFlag(List<Long> defaultValue) {
    return new Flag.LongsListFlag(defaultValue);
  }

  public static Flag<List<Double>> createDoublesListFlag(List<Double> defaultValue) {
    return new Flag.DoublesListFlag(defaultValue);
  }

  private static class StringFlag extends Flag<String> {
    StringFlag(@Nonnull String defaultValue) {
      super(defaultValue);
    }

    @Override
    String parse(@Nonnull String value) {
      return String.valueOf(value);
    }
  }

  private static class BooleanFlag extends Flag<Boolean> {
    BooleanFlag(@Nonnull Boolean defaultValue) {
      super(defaultValue);
    }

    @Override
    Boolean parse(@Nonnull String value) {
      return Boolean.valueOf(value);
    }
  }

  private static class IntegerFlag extends Flag<Integer> {
    IntegerFlag(@Nonnull Integer defaultValue) {
      super(defaultValue);
    }

    @Override
    Integer parse(@Nonnull String value) {
      return Integer.valueOf(value);
    }
  }

  private static class LongFlag extends Flag<Long> {
    LongFlag(@Nonnull Long defaultValue) {
      super(defaultValue);
    }

    @Override
    Long parse(@Nonnull String value) {
      return Long.valueOf(value);
    }
  }

  private static class DoubleFlag extends Flag<Double> {
    DoubleFlag(@Nonnull Double defaultValue) {
      super(defaultValue);
    }

    @Override
    Double parse(@Nonnull String value) {
      return Double.valueOf(value);
    }
  }

  private static class StringsListFlag extends Flag<List<String>> {
    StringsListFlag(@Nonnull List<String> defaultValue) {
      super(defaultValue);
    }

    @Override
    List<String> parse(@Nonnull String value) {
      return Arrays.stream(value.split(","))
          .map(String::valueOf)
          .collect(Collectors.toList());
    }
  }

  private static class BooleansListFlag extends Flag<List<Boolean>> {
    BooleansListFlag(@Nonnull List<Boolean> defaultValue) {
      super(defaultValue);
    }

    @Override
    List<Boolean> parse(@Nonnull String value) {
      return Arrays.stream(value.split(","))
          .map(Boolean::valueOf)
          .collect(Collectors.toList());
    }
  }

  private static class IntegersListFlag extends Flag<List<Integer>> {
    IntegersListFlag(@Nonnull List<Integer> defaultValue) {
      super(defaultValue);
    }

    @Override
    List<Integer> parse(@Nonnull String value) {
      return Arrays.stream(value.trim().split(","))
          .map(Integer::valueOf)
          .collect(Collectors.toList());
    }
  }

  private static class LongsListFlag extends Flag<List<Long>> {
    LongsListFlag(@Nonnull List<Long> defaultValue) {
      super(defaultValue);
    }

    @Override
    List<Long> parse(@Nonnull String value) {
      return Arrays.stream(value.trim().split(","))
          .map(Long::valueOf)
          .collect(Collectors.toList());
    }
  }

  private static class DoublesListFlag extends Flag<List<Double>> {
    DoublesListFlag(@Nonnull List<Double> defaultValue) {
      super(defaultValue);
    }

    @Override
    List<Double> parse(@Nonnull String value) {
      return Arrays.stream(value.trim().split(","))
          .map(Double::valueOf)
          .collect(Collectors.toList());
    }
  }

  protected T defaultValue;
  protected T value;
  protected String name;
  protected boolean required;

  public Flag(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  abstract T parse(@Nonnull String value);

  // This accessor is only used to get the initial default value, which is then
  // stored in Flags.
  T getDefault() {
    return defaultValue;
  }

  void setName(String name) {
    this.name = name;
  }

  void setRequired(boolean required) {
    this.required = required;
  }

  public void resetValueForTesting() {
    value = null;
  }

  public T get() {
    T prevValue = value;
    if (Flags.getFlagValue(name) != null) {
      value = parse(Flags.getFlagValue(name));
    } else {
      value = parse(Flags.getDefaultFlagValue(name));
    }
    Flags.setFlagValue(name, value.toString());
    if (prevValue != null && !prevValue.equals(value)) {
      log.atSevere()
              .log("Flag value has changed between get() calls. Previous value is %s and current is %s",
              prevValue,
              value);
    }
    if (required && value.equals(Flags.getDefaultFlagValue(name))) {
      throw new IllegalArgumentException(
          String.format("Argument '%s' is required but was not supplied", name));
    }
    return value;
  }
}
