import { Difference, Line } from '@/shared';
import { Injectable } from '@angular/core';

@Injectable()
export class DifferenceService {

  // implementation of algo found here
  // https://en.wikipedia.org/wiki/Longest_common_subsequence_problem

  /*
  * Compute the Difference of each line of the two files. It iterates over
  *   the file and compute difference by comparing line i to line i
  * @param { Array<string> } c1 The contents of file1, each line as a string
  * @param { Array<string> } c2 The contents of file2, each line as a string
  * @return An array of Differences that is used by component to print
  *   out the insertions in green and deletions in red
  */
  public computeDiff(
    c1: Array<string>,
    c2: Array<string>
  ): Array<Array<Difference>> {
    const x: Array<Array<Difference>> = [];
    for (let i = 0; i < c1.length; i++) {
      x.push(this.computeDifferences(c1[i], c2[i]));
    }
    return x;
  }

  /*
  * Call a function that computes the matrix of LCS
  *   It is the first step to find out the LCS
  * @param { string } X line from file1 as a string
  * @param { string } Y line from file2 as a string
  * @return An array of Differences that is used by component to print
  *   out the insertions in green and deletions in red
  */
  private computeDifferences(X: string, Y: string): Array<Difference> {
    return this.computeMatrix(X, Y);
  }

  /*
  * The function below takes as strings X[1..m] and Y[1..n]
  * computes the LCS between X[1..i] and Y[1..j]
  * for all 1 ≤ i ≤ m and 1 ≤ j ≤ n, and stores it in C[i,j].
  * C will contain the table of the length of the subsequence of X and Y.
  *
  * @param { string } X line from file1 as a string
  * @param { string } Y line from file2 as a string
  * @return An array of Differences that is used by component to print
  *   out the insertions in green and deletions in red
  */
  private computeMatrix(X: string, Y: string): Array<Difference> {
    const C = [];
    /*
    * The following for loop populates the matrix with
    * zeros forming a matrix that looks like below
    *
    * if X = GAC and Y = AGCAT
    * so X.length = 3 and Y.legth = 5
    *
    *       0 0 0 0 0 0
    *  C =  0 0 0 0 0 0
    *       0 0 0 0 0 0
    *       0 0 0 0 0 0
    */
    for (let x = 0; x <= X.length; x++) {
      const arr = new Array(Y.length + 1).fill(0);
      C.push(arr);
    }
    /*
    * if X = GAC and Y = AGCAT
    *
    * The following for loop populates the matrix with
    * if X[0] === Y[0]
    *    C[1][1] is set to C[0][0] + 1
    *  else
    *   C[1][1] is set to Max of C[1][0] and C[0][1]
    *
    * and so on.
    *
    *       0 0 0 0 0 0
    *  C =  0 0 1 1 1 1
    *       0 1 1 1 2 2
    *       0 1 1 2 2 2
    */
    for (let i = 1; i <= X.length; i++) {
      for (let j = 1; j <= Y.length; j++) {
        C[i][j] =
          X[i - 1] === Y[j - 1]
            ? C[i - 1][j - 1] + 1
            : Math.max(C[i][j - 1], C[i - 1][j]);
      }
    }
    const XLength = X.length;
    const YLength = Y.length;
    return this.getPrintableDiff(C, X, Y, XLength, YLength);
  }

  /*
  * This function will backtrack through the C matrix,
  * and form an array depicting the diff between the two sequences.
  *
  * @param { Array<Array<Number>> } The matrix C
  *   that matrix is used to store the LCS sequence
  * @param { string } X line from file1 as a string
  * @param { string } Y line from file2 as a string
  * @param { number } i length of line X
  * @param { number } j length of line Y
  * @return An array of Diff that is used by component to print
  *   out the insertions in green and deletions in red.
  * In case of X = GAC and Y = AGCAT the retured value is
  *   [ {operation: "-", character: "G"},
  *     {operation: " ", character: "A"},
  *     {operation: "+", character: "G"},
  *     {operation: " ", character: "C"},
  *     {operation: "+", character: "A"},
  *     {operation: "+", character: "T"}
  *   ]
  */
  private getPrintableDiff(
    C: Array<Array<Number>>,
    X: string,
    Y: string,
    i: number,
    j: number
  ): Array<Difference> {
    let a: Array<Difference>;
    if (i > 0 && j > 0 && X[i - 1] === Y[j - 1]) {
      // if i and j are still positive and
      // x and y value match backtrack diagonally
      // into the matrix
      a = this.getPrintableDiff(C, X, Y, i - 1, j - 1);
      a.push({ operation: ' ', character: X[i - 1] });
      return a;
    } else if (j > 0 && (i === 0 || C[i][j - 1] >= C[i - 1][j])) {
      // if j is still positive and
      // C[i][j -1] is greater than equal to C[i - 1][j]
      // backtrack horizontally in to the matrix
      a = this.getPrintableDiff(C, X, Y, i, j - 1);
      a.push({ operation: '+', character: Y[j - 1] });
      return a;
    } else if (i > 0 && (j === 0 || C[i][j - 1] < C[i - 1][j])) {
      // if j is still positive and
      // C[i][j -1] is less than C[i - 1][j]
      // backtrack vertically in to the matrix
      a = this.getPrintableDiff(C, X, Y, i - 1, j);
      a.push({ operation: '-', character: X[i - 1] });
      return a;
    }
    return [];
  }
}
