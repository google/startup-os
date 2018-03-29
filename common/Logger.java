package com.google.startupos.common;

import java.lang.StackTraceElement;
import org.slf4j.LoggerFactory;

/*
 * A wrapper for slf4j logger.
 *
 * Reasons for wrapping:
 * - We only need one package import instead of 2.
 * - We don't need to add a dependency (it's included in the common dep).
 * - Log initialization can fit in one line.
 * - Can add single letter logging - d() for debug() etc.
 * - Moving to a different logger in the future is easier.
 * - Easier for someone else to use our code, that may use a different logger.
 * - Can create convenient methods like getForClass().
 *
 * Access to the internal slf4j logger is through getInternalLogger.
 */
// TODO: Use getForClass where appropriate.
public class Logger {
  private org.slf4j.Logger logger;

  public static Logger get(Class<?> clazz) {
    return new Logger(clazz);
  }

  public static Logger getForClass() {
    try {
      String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
      String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
      return new Logger(className);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException("Cannot find class name in stack", e);
    }
  }

  private Logger(Class<?> clazz) {
    logger = LoggerFactory.getLogger(clazz);
  }

  private Logger(String name) {
    logger = LoggerFactory.getLogger(name);
  }

  public org.slf4j.Logger getInternalLogger() {
    return logger;
  }

  public void trace(String msg) {
    logger.trace(msg);
  }

  public void t(String msg) {
    logger.trace(msg);
  }

  public void trace(String format, Object arg) {
    logger.trace(format, arg);
  }

  public void t(String format, Object arg) {
    logger.trace(format, arg);
  }

  public void trace(String format, Object arg1, Object arg2) {
    logger.trace(format, arg1, arg2);
  }

  public void t(String format, Object arg1, Object arg2) {
    logger.trace(format, arg1, arg2);
  }

  public void trace(String format, Object... arguments) {
    logger.trace(format, arguments);
  }

  public void t(String format, Object... arguments) {
    logger.trace(format, arguments);
  }

  public void trace(String msg, Throwable t) {
    logger.trace(msg, t);
  }

  public void t(String msg, Throwable t) {
    logger.trace(msg, t);
  }

  public void debug(String msg) {
    logger.debug(msg);
  }

  public void d(String msg) {
    logger.debug(msg);
  }

  public void debug(String format, Object arg) {
    logger.debug(format, arg);
  }

  public void d(String format, Object arg) {
    logger.debug(format, arg);
  }

  public void debug(String format, Object arg1, Object arg2) {
    logger.debug(format, arg1, arg2);
  }

  public void d(String format, Object arg1, Object arg2) {
    logger.debug(format, arg1, arg2);
  }

  public void debug(String format, Object... arguments) {
    logger.debug(format, arguments);
  }

  public void d(String format, Object... arguments) {
    logger.debug(format, arguments);
  }

  public void debug(String msg, Throwable t) {
    logger.debug(msg, t);
  }

  public void d(String msg, Throwable t) {
    logger.debug(msg, t);
  }

  public void info(String msg) {
    logger.info(msg);
  }

  public void i(String msg) {
    logger.info(msg);
  }

  public void info(String format, Object arg) {
    logger.info(format, arg);
  }

  public void i(String format, Object arg) {
    logger.info(format, arg);
  }

  public void info(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void i(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void i(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void info(String msg, Throwable t) {
    logger.info(msg, t);
  }

  public void i(String msg, Throwable t) {
    logger.info(msg, t);
  }

  public void warn(String msg) {
    logger.info(msg);
  }

  public void w(String msg) {
    logger.info(msg);
  }

  public void warn(String format, Object arg) {
    logger.info(format, arg);
  }

  public void w(String format, Object arg) {
    logger.info(format, arg);
  }

  public void warn(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void w(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void warn(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void w(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void warn(String msg, Throwable t) {
    logger.info(msg, t);
  }

  public void w(String msg, Throwable t) {
    logger.info(msg, t);
  }

  public void error(String msg) {
    logger.info(msg);
  }

  public void e(String msg) {
    logger.info(msg);
  }

  public void error(String format, Object arg) {
    logger.info(format, arg);
  }

  public void e(String format, Object arg) {
    logger.info(format, arg);
  }

  public void error(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void e(String format, Object arg1, Object arg2) {
    logger.info(format, arg1, arg2);
  }

  public void error(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void e(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void error(String msg, Throwable t) {
    logger.info(msg, t);
  }

  public void e(String msg, Throwable t) {
    logger.info(msg, t);
  }
}

