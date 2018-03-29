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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
// TODO: Make logging only require 1 package instead of 2.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses command-line arguments.
 *
 * <p>Implementation is based on Google's gflags: https://gflags.github.io/gflags
 *
 * <p>Note about method names - handleXArg methods are for reading arguments, while processX are for
 * doing something with them like maintaining the current state and writing flag data.
 */
class GflagsParser {
  private static final Logger log = LoggerFactory.getLogger(GflagsParser.class);
  private final List<String> arguments = new ArrayList<String>();
  private final Map<String, FlagData> flags;
  private State state = State.EXPECT_KEY;
  private FlagData lastFlag;

  enum State {
    EXPECT_KEY,
    EXPECT_VALUE,
    EXPECT_BOTH
  }

  public GflagsParser(@Nonnull Map<String, FlagData> flags) {
    this.flags = flags;
  }

  public ParseResult parse(String[] args) {
    for (String arg : args) {
      handleArg(arg);
    }
    if (state == State.EXPECT_VALUE) {
      errorFlagHasNoValue();
    }
    return new ParseResult(flags, arguments);
  }

  void handleArg(String arg) {
    if (arg.startsWith("--")) {
      handleFlagArg(arg.substring(2), arg);
    } else if (arg.startsWith("-")) {
      handleFlagArg(arg.substring(1), arg);
    } else {
      processValue(arg);
    }
  }

  void handleFlagArg(String flag, String arg) {
    String[] tokens = flag.split("=", 2);
    if (tokens.length == 1) {
      handleKeyArg(flag, arg);
    } else {
      String key = tokens[0];
      String value = tokens[1];
      handleKeyAndValueArg(key, value, arg);
    }
  }

  void handleKeyArg(String key, String arg) {
    if (state == State.EXPECT_VALUE) {
      errorFlagHasNoValue();
    }
    FlagData flagData = flags.get(key);
    if (flagData == null) {
      if (key.startsWith("no")) {
        flagData = flags.get(key.substring(2));
        if (flagData != null) {
          processFalseFlag(flagData, arg);
        } else {
          errorUnknownFlag(arg);
        }
      } else {
        errorUnknownFlag(arg);
      }
    } else {
      processFlag(flagData);
    }
  }

  void handleKeyAndValueArg(String key, String value, String arg) {
    if (state == State.EXPECT_VALUE) {
      errorFlagHasNoValue();
    }
    FlagData flagsMetadata = flags.get(key);
    if (flagsMetadata != null) {
      processFlag(flagsMetadata);
      processValue(value);
    } else {
      errorUnknownFlag(arg);
    }
  }

  void processFlag(FlagData flag) {
    if (flag.getIsBooleanFlag()) {
      Flags.setFlagValue(flag.getName(), "true");
      flag = Flags.getFlag(flag.getName());
      state = State.EXPECT_BOTH;
    } else {
      state = State.EXPECT_VALUE;
    }
    this.lastFlag = flag;
  }

  void processFalseFlag(FlagData flag, String arg) {
    if (flag.getIsBooleanFlag()) {
      Flags.setFlagValue(flag.getName(), "false");
      flag = Flags.getFlag(flag.getName());
    } else {
      errorUnknownFlag(arg);
    }
  }

  void processValue(String value) {
    if (state == State.EXPECT_VALUE
        || (state == State.EXPECT_BOTH && lastFlag.getIsBooleanFlag())) {
      lastFlag = lastFlag.toBuilder().setValue(value).build();
      Flags.setFlagValue(lastFlag.getName(), value);
      state = State.EXPECT_KEY;
    } else {
      arguments.add(value);
    }
  }

  private void errorUnknownFlag(String flag) {
    log.error("Unknown flag: {}", flag);
  }

  private void errorFlagHasNoValue() {
    log.error("Option {} has no value", lastFlag);
  }

  @VisibleForTesting
  void setState(State state) {
    this.state = state;
  }

  public class ParseResult {
    public Map<String, FlagData> flags;
    public List<String> unusedArgs;

    public ParseResult(Map<String, FlagData> flags, List<String> unusedArgs) {
      this.flags = flags;
      this.unusedArgs = unusedArgs;
    }
  }
}

