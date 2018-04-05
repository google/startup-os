package com.google.startup.common;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class CharDifference {
  private int index;
  private char difference;
  private DifferenceType differenceType;

  public CharDifference(int index, char difference, DifferenceType type) {
    this.index = index;
    this.difference = difference;
    this.differenceType = type;
  }

  public DifferenceType getDifferenceType() {
    return this.differenceType;
  }

  public int getIndex() {
    return this.index;
  }

  public char getCharDifference() {
    return this.difference;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || !getClass().equals(other.getClass())) {
      return false;
    }
    CharDifference charDifference = (CharDifference) other;
    return Objects.equals(this.index, charDifference.index)
        && Objects.equals(this.difference, charDifference.difference)
        && Objects.equals(this.differenceType, charDifference.differenceType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.index, this.difference, this.differenceType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("index", this.index)
        .add("difference", this.difference)
        .add("differenceType", this.differenceType)
        .toString();
  }
}
