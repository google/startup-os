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

