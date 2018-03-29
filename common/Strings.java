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

 package com.google.startupos.common;

import org.apache.commons.lang3.StringUtils;

/**
 * String utils
 *
 * <p>The reason for this wrapper is that there seems to be a couple of string util libraries
 * (Apache, Guava) and we're not sure if one supports all our use-cases. In case of future change,
 * it'll be easier to change the code here than in all calling sites.
 */
public class Strings {

  public static String capitalize(String string) {
    return StringUtils.capitalize(string);
  }
}

