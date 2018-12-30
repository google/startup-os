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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;

/** List utils. */
public class Lists {

  /**
   * Splits list into consecutive segments of numbers. Assumed all numbers are unique and
   * monotonously increasing.
   */
  public static ImmutableList<Segment> splitToSegments(List<Integer> list) {
    ImmutableList.Builder<Segment> result = ImmutableList.builder();
    if (list.isEmpty()) {
      return ImmutableList.of();
    }
    if (list.size() == 1) {
      return ImmutableList.of(Segment.create(0, list.get(0)));
    }
    int segmentStartIndex = 0;
    for (int i = 0; i < list.size() - 1; i++) {
      int currentValue = list.get(i);
      int nextValue = list.get(i + 1);
      if (nextValue - currentValue > 1) {
        result.add(Segment.create(segmentStartIndex, list.get(segmentStartIndex), i, currentValue));
        segmentStartIndex = i + 1;
      }
    }
    // Add last segment:
    if (list.get(list.size() - 1) - list.get(list.size() - 2) > 1) {
      // Add size=1 segment at last index:
      result.add(Segment.create(list.size() - 1, list.get(list.size() - 1)));
    } else {
      // Add size>1 segment:
      result.add(
          Segment.create(
              segmentStartIndex,
              list.get(segmentStartIndex),
              list.size() - 1,
              list.get(list.size() - 1)));
    }
    return result.build();
  }

  @AutoValue
  public abstract static class Segment {
    // end index is inclusive
    static Segment create(int startIndex, int startValue, int endIndex, int endValue) {
      return new AutoValue_Lists_Segment(startIndex, startValue, endIndex, endValue);
    }

    static Segment create(int index, int value) {
      return new AutoValue_Lists_Segment(index, value, index, value);
    }

    abstract int startIndex();

    abstract int startValue();

    abstract int endIndex();

    abstract int endValue();
  }
}

