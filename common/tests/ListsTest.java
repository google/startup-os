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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.startupos.common.Lists.Segment;
import org.junit.Test;

public class ListsTest {
  @Test
  public void splitToSegments_emptyListTest() {
    assertEquals(0, Lists.splitToSegments(ImmutableList.of()).size());
  }

  @Test
  public void splitToSegments_singleNumberTest() {
    ImmutableList<Segment> output = Lists.splitToSegments(ImmutableList.of(1));
    ImmutableList<Segment> expectedOutput = ImmutableList.of(Segment.create(0, 1));
    assertEquals(1, output.size());
    assertEquals(expectedOutput, output);
  }

  @Test
  public void splitToSegments_singleSegmentTest() {
    ImmutableList<Segment> output = Lists.splitToSegments(ImmutableList.of(5, 6));
    ImmutableList<Segment> expectedOutput = ImmutableList.of(Segment.create(0, 5, 1, 6));
    assertEquals(1, output.size());
    assertEquals(expectedOutput, output);
  }

  @Test
  public void splitToSegments_multipleSegmentsTest() {
    ImmutableList<Segment> output = Lists.splitToSegments(ImmutableList.of(5, 9, 10, 20, 30));
    ImmutableList<Segment> expectedOutput =
        ImmutableList.of(
            Segment.create(0, 5),
            Segment.create(1, 9, 2, 10),
            Segment.create(3, 20),
            Segment.create(4, 30));
    assertEquals(expectedOutput.size(), output.size());
    assertEquals(expectedOutput, output);
  }

  @Test
  public void splitToSegments_multipleSegments2Test() {
    ImmutableList<Segment> output =
        Lists.splitToSegments(ImmutableList.of(5, 6, 7, 9, 10, 20, 30, 31));
    ImmutableList<Segment> expectedOutput =
        ImmutableList.of(
            Segment.create(0, 5, 2, 7),
            Segment.create(3, 9, 4, 10),
            Segment.create(5, 20),
            Segment.create(6, 30, 7, 31));
    assertEquals(expectedOutput.size(), output.size());
    assertEquals(expectedOutput, output);
  }
}

