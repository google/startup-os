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

package com.google.startupos.tools.reviewer.job.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineNumberConverter {

  private Map<Integer, Integer> positionToLineNumberLeftSide = new HashMap<>();
  private Map<Integer, Integer> positionToLineNumberRightSide = new HashMap<>();
  private Map<Integer, Integer> lineNumberToPositionLeftSide = new HashMap<>();
  private Map<Integer, Integer> lineNumberToPositionRightSide = new HashMap<>();

  public LineNumberConverter(String diffPatchString) {
    processDiffPatch(diffPatchString);
  }

  private void processDiffPatch(String diffPatchStr) {
    Patch patch = new Patch(diffPatchStr);
    List<String> newLineSymbols = patch.getNewlineSymbols();
    List<Patch.DiffHunkHeader> diffHunkHeaders = patch.getDiffHunkHeaders();
    int diffHunkIndex = 0;
    int positionIndex = 1;

    positionToLineNumberLeftSide.put(
        positionIndex, diffHunkHeaders.get(diffHunkIndex).getLeftStartLine());
    positionToLineNumberRightSide.put(
        positionIndex, diffHunkHeaders.get(diffHunkIndex).getRightStartLine());
    lineNumberToPositionLeftSide.put(
        diffHunkHeaders.get(diffHunkIndex).getLeftStartLine(), positionIndex);
    lineNumberToPositionRightSide.put(
        diffHunkHeaders.get(diffHunkIndex).getRightStartLine(), positionIndex);
    positionIndex++;

    for (String n : newLineSymbols) {
      int lastLeftLineNumber =
          positionToLineNumberLeftSide.get(positionToLineNumberLeftSide.size());
      int lastRightLineNumber =
          positionToLineNumberRightSide.get(positionToLineNumberRightSide.size());
      switch (n) {
        case "\n":
          {
            ++diffHunkIndex;
            positionToLineNumberLeftSide.put(
                positionIndex, diffHunkHeaders.get(diffHunkIndex).getLeftStartLine());
            positionToLineNumberRightSide.put(
                positionIndex, diffHunkHeaders.get(diffHunkIndex).getRightStartLine());
            lineNumberToPositionLeftSide.put(
                diffHunkHeaders.get(diffHunkIndex).getLeftStartLine(), positionIndex);
            lineNumberToPositionRightSide.put(
                diffHunkHeaders.get(diffHunkIndex).getRightStartLine(), positionIndex);
            positionIndex++;
            break;
          }
        case "\n ":
          {
            positionToLineNumberLeftSide.put(positionIndex, lastLeftLineNumber + 1);
            positionToLineNumberRightSide.put(positionIndex, lastRightLineNumber + 1);
            lineNumberToPositionLeftSide.put(lastLeftLineNumber + 1, positionIndex);
            lineNumberToPositionRightSide.put(lastRightLineNumber + 1, positionIndex);
            positionIndex++;
            break;
          }
        case "\n-":
          {
            positionToLineNumberLeftSide.put(positionIndex, lastLeftLineNumber + 1);
            positionToLineNumberRightSide.put(positionIndex, lastRightLineNumber);
            lineNumberToPositionLeftSide.put(lastLeftLineNumber + 1, positionIndex);
            lineNumberToPositionRightSide.put(lastRightLineNumber, positionIndex);
            positionIndex++;
            break;
          }
        case "\n+":
          {
            positionToLineNumberLeftSide.put(positionIndex, lastLeftLineNumber);
            positionToLineNumberRightSide.put(positionIndex, lastRightLineNumber + 1);
            lineNumberToPositionLeftSide.put(lastLeftLineNumber, positionIndex);
            lineNumberToPositionRightSide.put(lastRightLineNumber + 1, positionIndex);
            positionIndex++;
            break;
          }
      }
    }
  }

  public int getLineNumb(int position, Side side) {
    return side.equals(Side.LEFT)
        ? positionToLineNumberLeftSide.get(position)
        : positionToLineNumberRightSide.get(position);
  }

  public int getPosition(int lineNumber, Side side) {
    return side.equals(Side.LEFT)
        ? lineNumberToPositionLeftSide.get(lineNumber)
        : lineNumberToPositionRightSide.get(lineNumber);
  }

  public enum Side {
    LEFT,
    RIGHT
  }
}

