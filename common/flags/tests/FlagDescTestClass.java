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

package com.google.startupos.common.flags.testpackage1;

import com.google.startupos.common.flags.Flag;
import com.google.startupos.common.flags.FlagDesc;
import java.util.Arrays;
import java.util.List;

public class FlagDescTestClass {
  @SuppressWarnings("unused")
  @FlagDesc(name = "string_flag", description = "A flag description")
  private static final Flag<String> stringFlag = Flag.create("default value");

  @SuppressWarnings("unused")
  @FlagDesc(name = "boolean_flag", description = "A flag description")
  public static final Flag<Boolean> booleanFlag = Flag.create(true);

  @SuppressWarnings("unused")
  @FlagDesc(name = "integer_flag", description = "A flag description")
  public static final Flag<Integer> integerFlag = Flag.create(123);

  @SuppressWarnings("unused")
  @FlagDesc(name = "long_flag", description = "A flag description")
  public static final Flag<Long> longFlag = Flag.create(123456789L);

  @SuppressWarnings("unused")
  @FlagDesc(name = "double_flag", description = "A flag description")
  public static final Flag<Double> doubleFlag = Flag.create(1.23);

  @SuppressWarnings("unused")
  @FlagDesc(name = "strings_list_flag", description = "A flag description")
  public static final Flag<List<String>> stringsListFlag
      = Flag.createStringsListFlag(Arrays.asList("ab", "cd"));

  @SuppressWarnings("unused")
  @FlagDesc(name = "booleans_list_flag", description = "A flag description")
  public static final Flag<List<Boolean>> booleansListFlag
      = Flag.createBooleansListFlag(Arrays.asList(true, false));

  @SuppressWarnings("unused")
  @FlagDesc(name = "integers_list_flag", description = "A flag description")
  public static final Flag<List<Integer>> integersListFlag
      = Flag.createIntegersListFlag(Arrays.asList(1, -2));

  @SuppressWarnings("unused")
  @FlagDesc(name = "longs_list_flag", description = "A flag description")
  public static final Flag<List<Long>> longsListFlag
      = Flag.createLongsListFlag(Arrays.asList(123456789L, -123123123L));

  @SuppressWarnings("unused")
  @FlagDesc(name = "doubles_list_flag", description = "A flag description")
  public static final Flag<List<Double>> doublesListFlag
      = Flag.createDoublesListFlag(Arrays.asList(1.23, -3.21));

  @SuppressWarnings("unused")
  @FlagDesc(name = "required_flag", description = "A flag description", required = true)
  public static final Flag<String> requiredFlag = Flag.create("");

  @SuppressWarnings("unused")
  @FlagDesc(name = "strings_list_with_comma_flag", description = "A flag description")
  public static final Flag<List<String>> stringsListWithCommaFlag
      = Flag.createStringsListFlag(Arrays.asList("a, b", "c,d"));

  @SuppressWarnings("unused")
  public static final String notAFlag = "";

  public static String getStringFlagValue() {
    return stringFlag.get();
  }
}
