import { DiffLine, Thread } from '@/core/proto';

export enum BlockIndex {
  leftFile,
  rightFile,
}

// Code line of a block of code
export interface BlockLine {
  clearCode: string;
  // Code with highlighting
  code: string;
  wordChanges: string;
  lineNumber: number;
  // Is the line a placeholder?
  isPlaceholder: boolean;
  threads: Thread[];
  isChanged: boolean;
  diffLine: DiffLine;
}

// Line of code changes (left and right blocks)
export interface ChangesLine {
  // We keep it as an array to display blocks by ngFor
  blocks: BlockLine[];
  // Is the line a line with comments?
  isCommentsLine: boolean;
  commentsLine: ChangesLine;
}

export interface Dictionary {
  [id: number]: number;
}

export interface CodeGroup {
  changes: ChangesLine[];
  // To get index by line number
  map: Dictionary[];
  isExpandUpVisible: boolean;
  isExpandDownVisible: boolean;
}

export interface Section {
  startLineNumber: number;
  endLineNumber: number;
}
