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

package com.google.startupos.common.flags.testpackage;

import com.google.startupos.common.flags.Flags;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FlagsTest {
  private static final String TESTFLAGS_PACKAGE = FlagDescTestClass.class.getPackage().getName();
  private static final String FLAG_SHOULD_HAVE_VALUE = "Flag should have value";
  private static final String LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE =
      "List flag should have an element with value";

  @Before
  public void setup() {
    if (Flags.getFlags() != null) {
      Flags.getFlags().clear();
    }
  }

  @Test
  public void defaultsTest() {
    List<String> leftOverArgs = Arrays.asList(Flags.parse(new String[0], TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, "default value", FlagDescTestClass.getStringFlagValue());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, true, FlagDescTestClass.booleanFlag.get());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(123), FlagDescTestClass.integerFlag.get());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Long(123456789L), FlagDescTestClass.longFlag.get());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Double(1.23), FlagDescTestClass.doubleFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void listDefaultsTest() {
    List<String> leftOverArgs = Arrays.asList(Flags.parse(new String[] {}, TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE, Arrays.asList("ab", "cd"), FlagDescTestClass.stringsListFlag.get());
    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(true, false),
        FlagDescTestClass.booleansListFlag.get());
    assertEquals(
        FLAG_SHOULD_HAVE_VALUE, Arrays.asList(1, -2), FlagDescTestClass.integersListFlag.get());
    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(123456789L, -123123123L),
        FlagDescTestClass.longsListFlag.get());
    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(1.23, -3.21),
        FlagDescTestClass.doublesListFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void getItemFromDefaultValuesListTest() {
    List<String> leftOverArgs = Arrays.asList(Flags.parse(new String[] {}, TESTFLAGS_PACKAGE));

    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        "ab",
        FlagDescTestClass.stringsListFlag.get().get(0));
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        false,
        FlagDescTestClass.booleansListFlag.get().get(1));
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        1,
        (int) FlagDescTestClass.integersListFlag.get().get(0));
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        -123123123L,
        (long) FlagDescTestClass.longsListFlag.get().get(1));
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        1.23,
        FlagDescTestClass.doublesListFlag.get().get(0),
        0.0);
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void onlyFlagTest() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--string_flag", "some value"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, "some value", FlagDescTestClass.getStringFlagValue());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void onlyArgsTest() {
    List<String> nonFlags =
        Arrays.asList(Flags.parse(new String[] {"just", "args"}, TESTFLAGS_PACKAGE));

    assertEquals(
        "Should inject flag value", "default value", FlagDescTestClass.getStringFlagValue());
    assertArrayEquals(
        "Arguments should be: just, args",
        nonFlags.toArray(new String[0]),
        new String[] {"just", "args"});
  }

  @Test
  public void flagsThenArgsTest() {
    List<String> nonFlags =
        Arrays.asList(
            Flags.parse(
                new String[] {"--string_flag", "some value", "just", "args"}, TESTFLAGS_PACKAGE));

    assertEquals("Should inject flag value", "some value", FlagDescTestClass.getStringFlagValue());
    assertArrayEquals(
        "Arguments should be: just, args",
        nonFlags.toArray(new String[0]),
        new String[] {"just", "args"});
  }

  @Test
  public void firstArgsThenFlagsTest() {
    List<String> nonFlags =
        Arrays.asList(
            Flags.parse(
                new String[] {"just", "args", "--string_flag", "some value"}, TESTFLAGS_PACKAGE));

    assertEquals("Should inject flag value", "some value", FlagDescTestClass.getStringFlagValue());
    assertArrayEquals(
        "Arguments should be: just, args",
        nonFlags.toArray(new String[0]),
        new String[] {"just", "args"});
  }

  @Test
  public void printUsageTest() {
    PrintStream stdout = System.out;
    ByteArrayOutputStream catchStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(catchStream));

    Flags.parse(new String[0], TESTFLAGS_PACKAGE);
    Flags.printUsage();
    String result = new String(catchStream.toByteArray());

    System.setOut(stdout);
    System.out.print(result);

    assertTrue("Should print flag name", result.contains("string_flag"));
    assertTrue("Should print flag description", result.contains("A flag description"));
  }

  @Test
  public void testBooleanFlagSetToFalse() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--boolean_flag", "false"}, TESTFLAGS_PACKAGE));

    assertEquals("Flag should have false value", false, FlagDescTestClass.booleanFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testBooleanFlagSetToTrue() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--boolean_flag", "true"}, TESTFLAGS_PACKAGE));

    assertEquals("Flag should have true value", true, FlagDescTestClass.booleanFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testBooleanFlagSetToFalseUsingNoX() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--noboolean_flag"}, TESTFLAGS_PACKAGE));
    assertEquals("Flag should have false value", false, FlagDescTestClass.booleanFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testBooleanFlagSetToFalseUsingX() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--boolean_flag"}, TESTFLAGS_PACKAGE));
    assertEquals("Flag should have true value", true, FlagDescTestClass.booleanFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testStringFlag() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--string_flag", "abcd"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, "abcd", FlagDescTestClass.getStringFlagValue());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testStringFlagWithSquareScopes() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--string_flag", "[abcd]"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, "[abcd]", FlagDescTestClass.getStringFlagValue());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testIntegerFlag() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--integer_flag", "1234"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(1234), FlagDescTestClass.integerFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testLongFlag() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--long_flag", "987654321"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Long(987654321L), FlagDescTestClass.longFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testDoubleFlag() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--double_flag", "9.87"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Double(9.87), FlagDescTestClass.doubleFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testStringsListFlag() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(new String[] {"--strings_list_flag", "ab,cd,ef"}, TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList("ab", "cd", "ef"),
        FlagDescTestClass.stringsListFlag.get());
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        "cd",
        FlagDescTestClass.stringsListFlag.get().get(1));
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testBooleansListFlag() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(
                new String[] {"--booleans_list_flag", "false,true,false"}, TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(false, true, false),
        FlagDescTestClass.booleansListFlag.get());
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        false,
        FlagDescTestClass.booleansListFlag.get().get(0));
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testIntegersListFlag() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(new String[] {"--integers_list_flag", "1,2,-3"}, TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE, Arrays.asList(1, 2, -3), FlagDescTestClass.integersListFlag.get());
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        -3,
        (int) FlagDescTestClass.integersListFlag.get().get(2));
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testLongsListFlag() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(
                new String[] {"--longs_list_flag", "321321321,-123123123,987654321"},
                TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(321321321L, -123123123L, 987654321L),
        FlagDescTestClass.longsListFlag.get());
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        -123123123L,
        (long) FlagDescTestClass.longsListFlag.get().get(1));
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testDoublesListFlag() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(
                new String[] {"--doubles_list_flag", "3.21,-1.23,9.87"}, TESTFLAGS_PACKAGE));

    assertEquals(
        FLAG_SHOULD_HAVE_VALUE,
        Arrays.asList(3.21, -1.23, 9.87),
        FlagDescTestClass.doublesListFlag.get());
    assertEquals(
        LIST_FLAG_SHOULD_HAVE_ELEMENT_WITH_VALUE,
        3.21,
        FlagDescTestClass.doublesListFlag.get().get(0),
        0.0);
    assertEquals(0, leftOverArgs.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRequiredStringFlagNotSupplied() {
    Flags.parse(new String[] {}, TESTFLAGS_PACKAGE);

    FlagDescTestClass.requiredFlag.get();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRequiredStringFlagNoValue() {
    Flags.parse(new String[] {"--required_flag"}, TESTFLAGS_PACKAGE);

    FlagDescTestClass.requiredFlag.get();
  }

  @Test
  public void testRequiredStringFlagWithValue() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parse(new String[] {"--required_flag", "required"}, TESTFLAGS_PACKAGE));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, "required", FlagDescTestClass.requiredFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFlagThrowsWithoutParsing() {
    FlagDescTestClass.integerFlag.get();
  }

  @Test
  public void testFindExistingFlag_WithParsingParticularClass() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parse(new String[] {"--integer_flag", "1234"}, FlagDescTestClass.class));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(1234), FlagDescTestClass.integerFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testNotFailWhenClassDoesNotHaveAnyFlags_WithParsingParticularClass() {
    Flags.parse(new String[0], FlagTestClass.ClassWithoutFlags.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotTakeFlagInAnotherClass_WithParsingParticularClass() {
    Flags.parse(new String[0], FlagDescTestClass.class);

    FlagTestClass.stringTestFlag.get();
  }

  @Test
  public void testTakeFlagsInTwoClasses_WithParsingParticularClass() {
    Flags.parse(new String[0], FlagDescTestClass.class);
    Flags.parse(new String[0], FlagTestClass.class);

    assertEquals("default value", FlagDescTestClass.getStringFlagValue());
    assertEquals("default value", FlagTestClass.stringTestFlag.get());
  }

  @Test
  public void testParseCurrentPackage_fromOneClass() {
    List<String> leftOverArgs =
        Arrays.asList(Flags.parseCurrentPackage(new String[] {"--integer_flag", "12345"}));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(12345), FlagDescTestClass.integerFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testParseCurrentPackage_fromTwoClasses() {
    List<String> leftOverArgs =
        Arrays.asList(
            Flags.parseCurrentPackage(
                new String[] {"--integer_flag", "12345", "--string_test_flag", "some value"}));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(12345), FlagDescTestClass.integerFlag.get());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, "some value", FlagTestClass.stringTestFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testParseCurrentPackage_defaultValuesFromOneClass() {
    List<String> leftOverArgs = Arrays.asList(Flags.parseCurrentPackage(new String[0]));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(123), FlagDescTestClass.integerFlag.get());
    assertEquals(0, leftOverArgs.size());
  }

  @Test
  public void testParseCurrentPackage_defaultValuesFromTwoClasses() {
    List<String> leftOverArgs = Arrays.asList(Flags.parseCurrentPackage(new String[0]));

    assertEquals(FLAG_SHOULD_HAVE_VALUE, new Integer(123), FlagDescTestClass.integerFlag.get());
    assertEquals(FLAG_SHOULD_HAVE_VALUE, "default value", FlagTestClass.stringTestFlag.get());
    assertEquals(0, leftOverArgs.size());
  }
}

