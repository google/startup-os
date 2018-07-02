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

import static java.lang.Math.toIntExact;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/** Time utils */
// TODO: Consider using JODA time and maybe simplifying some methods.
public class Time {

  /**
   * Get calendar days between timestamps. For example, we return 1 for 23:00 and 1:00 the next day.
   */
  public static int daysBetween(long timeBeforeMs, long timeAfterMs) {
    return daysBetween(Instant.ofEpochMilli(timeBeforeMs), Instant.ofEpochMilli(timeAfterMs));
  }

  public static int daysBetween(Instant timeBefore, Instant timeAfter) {
    return toIntExact(DAYS.between(timeBefore, timeAfter));
  }

  /** Gets how many calendar days ago is the given time. */
  public static int daysAgo(long timeMs) {
    return daysAgo(Instant.ofEpochMilli(timeMs));
  }

  /** Gets how many calendar days ago is the given time. */
  public static int daysAgo(Instant time) {
    return daysBetween(time, Instant.now());
  }

  public static boolean isToday(long timeMs) {
    return daysAgo(timeMs) == 0;
  }

  public static boolean isYesterday(long timeMs) {
    return daysAgo(timeMs) == 1;
  }

  public static int hoursBetween(long timeBeforeMs, long timeAfterMs) {
    return hoursBetween(Instant.ofEpochMilli(timeBeforeMs), Instant.ofEpochMilli(timeAfterMs));
  }

  public static int hoursBetween(Instant timeBefore, Instant timeAfter) {
    return toIntExact(HOURS.between(timeBefore, timeAfter));
  }

  public static int minutesBetween(long timeBeforeMs, long timeAfterMs) {
    return minutesBetween(Instant.ofEpochMilli(timeBeforeMs), Instant.ofEpochMilli(timeAfterMs));
  }

  public static int minutesBetween(Instant timeBefore, Instant timeAfter) {
    return toIntExact(MINUTES.between(timeBefore, timeAfter));
  }

  public static String getHourMinuteString(long timeMs) {
    return format("h:mm a", timeMs);
  }

  public static String getHourMinuteDurationString(long timeMs) {
    Duration duration = Duration.of(timeMs, MILLIS);
    long hours = duration.toHours();
    long minutes = duration.minusHours(hours).toMinutes();
    if (hours == 0) {
      return String.format("%dmin", minutes);
    }
    return String.format("%dh %dmin", hours, minutes);
  }

  public static String format(String format, long timeMs) {
    DateFormat formatter = new SimpleDateFormat(format);
    return formatter.format(new Date(timeMs));
  }

  public static String getDayString(long timeMs) {
    Date date = new Date(timeMs);
    if (isToday(timeMs)) {
      return "today";
    } else if (isYesterday(timeMs)) {
      return "yesterday";
    }
    return format("EEE',' h:m a", timeMs);
  }

  public static String getDateString(long timeMs) {
    if (daysAgo(timeMs) < 7) {
      return getDayString(timeMs);
    } else {
      return format("EEE',' MMM d y',' h:m a", timeMs);
    }
  }

  /** Returns epoch millis assuming date is in UTC, at the start of the day. */
  public static long getMillis(LocalDate date) {
    return date.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
  }
}

