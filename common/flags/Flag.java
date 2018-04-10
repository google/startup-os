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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Flag class, with implementations for various flag types. */
// TODO: Add support for lists
public abstract class Flag<T> {
  private static final Logger log = LoggerFactory.getLogger(GflagsParser.class);

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

  public T get() {
    T prevValue = value;
    if (Flags.getFlagValue(name) != null) {
      value = parse(Flags.getFlagValue(name));
    } else {
      value = parse(Flags.getDefaultFlagValue(name));
    }
    Flags.setFlagValue(name, value.toString());
    if (prevValue != null && !prevValue.equals(value)) {
      log.error(
          String.format(
              "Flag value has changed between get() calls. "
                  + "Previous value is %s and current is %s",
              prevValue, value));
    }
    if (required && value.equals(Flags.getDefaultFlagValue(name))) {
      throw new IllegalArgumentException("Argument is required but was not supplied");
    }
    return value;
  }
}
