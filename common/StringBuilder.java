package com.google.startupos.common;

/**
 * StringBuilder wrapper
 *
 * <p>This wrapper adds the following: - appendln - like println, it adds a newline to the end of
 * the string. - append with format, so append(someString, arg1, arg2...) formats the someString
 * with the args.
 */
public class StringBuilder {
  private java.lang.StringBuilder sb;

  public StringBuilder() {
    sb = new java.lang.StringBuilder();
  }

  public StringBuilder(CharSequence seq) {
    sb = new java.lang.StringBuilder(seq);
  }

  public StringBuilder(int capacity) {
    sb = new java.lang.StringBuilder(capacity);
  }

  public StringBuilder(String str) {
    sb = new java.lang.StringBuilder(str);
  }

  /** Added methods: */
  public StringBuilder appendln() {
    sb = sb.append(System.lineSeparator());
    return this;
  }

  public StringBuilder appendln(String str) {
    sb = sb.append(str).append(System.lineSeparator());
    return this;
  }

  public StringBuilder appendln(String str, Object arg) {
    sb = sb.append(String.format(str, arg)).append(System.lineSeparator());
    return this;
  }

  public StringBuilder appendln(String str, Object arg1, Object arg2) {
    sb = sb.append(String.format(str, arg1, arg2)).append(System.lineSeparator());
    return this;
  }

  public StringBuilder appendln(String str, Object... arguments) {
    sb = sb.append(String.format(str, arguments)).append(System.lineSeparator());
    return this;
  }

  /** Original methods: */
  public StringBuilder append(boolean b) {
    sb = sb.append(b);
    return this;
  }

  public StringBuilder append(char c) {
    sb = sb.append(c);
    return this;
  }

  public StringBuilder append(char[] str) {
    sb = sb.append(str);
    return this;
  }

  public StringBuilder append(char[] str, int offset, int len) {
    sb = sb.append(str, offset, len);
    return this;
  }

  public StringBuilder append(CharSequence s) {
    sb = sb.append(s);
    return this;
  }

  public StringBuilder append(CharSequence s, int start, int end) {
    sb = sb.append(s, start, end);
    return this;
  }

  public StringBuilder append(double d) {
    sb = sb.append(d);
    return this;
  }

  public StringBuilder append(float f) {
    sb = sb.append(f);
    return this;
  }

  public StringBuilder append(int i) {
    sb = sb.append(i);
    return this;
  }

  public StringBuilder append(long lng) {
    sb = sb.append(lng);
    return this;
  }

  public StringBuilder append(Object obj) {
    sb = sb.append(obj);
    return this;
  }

  public StringBuilder append(String str) {
    sb = sb.append(str);
    return this;
  }

  public StringBuilder append(String str, Object arg) {
    sb = sb.append(String.format(str, arg));
    return this;
  }

  public StringBuilder append(String str, Object arg1, Object arg2) {
    sb = sb.append(String.format(str, arg1, arg2));
    return this;
  }

  public StringBuilder append(String str, Object... arguments) {
    sb = sb.append(String.format(str, arguments));
    return this;
  }

  public StringBuilder append(StringBuffer sb) {
    sb = sb.append(sb);
    return this;
  }

  public StringBuilder appendCodePoint(int codePoint) {
    sb = sb.appendCodePoint(codePoint);
    return this;
  }

  public int capacity() {
    return sb.capacity();
  }

  public char charAt(int index) {
    return sb.charAt(index);
  }

  public int codePointAt(int index) {
    return sb.codePointAt(index);
  }

  public int codePointBefore(int index) {
    return sb.codePointBefore(index);
  }

  public int codePointCount(int beginIndex, int endIndex) {
    return sb.codePointCount(beginIndex, endIndex);
  }

  public StringBuilder delete(int start, int end) {
    sb = sb.delete(start, end);
    return this;
  }

  public StringBuilder deleteCharAt(int index) {
    sb = sb.deleteCharAt(index);
    return this;
  }

  public void ensureCapacity(int minimumCapacity) {
    sb.ensureCapacity(minimumCapacity);
  }

  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    sb.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  public int indexOf(String str) {
    return sb.indexOf(str);
  }

  public int indexOf(String str, int fromIndex) {
    return sb.indexOf(str, fromIndex);
  }

  public StringBuilder insert(int offset, boolean b) {
    sb = sb.insert(offset, b);
    return this;
  }

  public StringBuilder insert(int offset, char c) {
    sb = sb.insert(offset, c);
    return this;
  }

  public StringBuilder insert(int offset, char[] str) {
    sb = sb.insert(offset, str);
    return this;
  }

  public StringBuilder insert(int index, char[] str, int offset, int len) {
    sb = sb.insert(index, str, offset, len);
    return this;
  }

  public StringBuilder insert(int dstOffset, CharSequence s) {
    sb = sb.insert(dstOffset, s);
    return this;
  }

  public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
    sb = sb.insert(dstOffset, s, start, end);
    return this;
  }

  public StringBuilder insert(int offset, double d) {
    sb = sb.insert(offset, d);
    return this;
  }

  public StringBuilder insert(int offset, float f) {
    sb = sb.insert(offset, f);
    return this;
  }

  public StringBuilder insert(int offset, int i) {
    sb = sb.insert(offset, i);
    return this;
  }

  public StringBuilder insert(int offset, long l) {
    sb = sb.insert(offset, l);
    return this;
  }

  public StringBuilder insert(int offset, Object obj) {
    sb = sb.insert(offset, obj);
    return this;
  }

  public StringBuilder insert(int offset, String str) {
    sb = sb.insert(offset, str);
    return this;
  }

  public int lastIndexOf(String str) {
    return sb.lastIndexOf(str);
  }

  public int lastIndexOf(String str, int fromIndex) {
    return sb.lastIndexOf(str, fromIndex);
  }

  public int length() {
    return sb.length();
  }

  public int offsetByCodePoints(int index, int codePointOffset) {
    return sb.offsetByCodePoints(index, codePointOffset);
  }

  public StringBuilder replace(int start, int end, String str) {
    sb = sb.replace(start, end, str);
    return this;
  }

  public StringBuilder reverse() {
    sb = sb.reverse();
    return this;
  }

  public void setCharAt(int index, char ch) {
    sb.setCharAt(index, ch);
  }

  public void setLength(int newLength) {
    sb.setLength(newLength);
  }

  public CharSequence subSequence(int start, int end) {
    return sb.subSequence(start, end);
  }

  public String substring(int start) {
    return sb.substring(start);
  }

  public String substring(int start, int end) {
    return sb.substring(start, end);
  }

  public String toString() {
    return sb.toString();
  }

  public void trimToSize() {
    sb.trimToSize();
  }
}

