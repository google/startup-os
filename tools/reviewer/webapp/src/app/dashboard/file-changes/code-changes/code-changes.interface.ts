import { TextChange, Thread } from '@/core/proto';

export enum BlockIndex {
  leftFile,
  rightFile,
}

export interface ThreadFrame {
  thread: Thread;
  // For future improvement.
  // Is "add new comment" interface focused by user cursor?
  // TODO: make it work
  isFocus: boolean;
}

// Code line of a block of code
export interface BlockLine {
  clearCode: string;
  // Code with highlighting
  code: string;
  lineNumber: number;
  // Is the line a placeholder?
  isPlaceholder: boolean;
  threadFrames: ThreadFrame[];
  isChanged: boolean;
  textChange?: TextChange;
}

// Line of code changes (left and right blocks)
export interface ChangesLine {
  // We keep it as an array to display blocks by ngFor
  blocks: BlockLine[];
  // Is the line a line with comments?
  isCommentsLine: boolean;
  commentsLine: ChangesLine;
}
