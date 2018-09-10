import { Injectable } from '@angular/core';

import {
  BlockIndex,
  ChangesLine,
} from '../code-changes.interface';

@Injectable()
export class CommentsService {
  // Line indexes of all open threads.
  openThreads: { [id: number]: number }[];

  constructor() {
    this.clearThreads();
  }

  // Display "add new comment" UI
  openComments(changesLine: ChangesLine, blockIndex: number): void {
    changesLine.commentsLine.blocks[blockIndex].isNewCommentVisible = true;
  }

  // Hide "add new comment" UI
  closeComments(changesLine: ChangesLine, blockIndex: number): void {
    changesLine.blocks[blockIndex].isNewCommentVisible = false;
  }

  // Save the thread as opened
  openThread(
    lineNumber: number,
    lineIndex: number,
    blockIndex: number,
  ): void {
    this.openThreads[blockIndex][lineNumber] = lineIndex;
  }

  // Remove from open threads
  closeThread(lineNumber: number, blockIndex: number): void {
    delete this.openThreads[blockIndex][lineNumber];
  }

  clearThreads(): void {
    this.openThreads = this.createSplitDictionary();
  }

  createSplitDictionary(): { [id: number]: number }[] {
    const openThreads: { [id: number]: number }[] = [];
    openThreads[BlockIndex.leftFile] = {};
    openThreads[BlockIndex.rightFile] = {};

    return openThreads;
  }

  copyOpenThreads(): { [id: number]: number }[] {
    const newOpenThreads: { [id: number]: number }[] = this
      .createSplitDictionary();
    newOpenThreads[BlockIndex.leftFile] = Object.assign(
      {},
      this.openThreads[BlockIndex.leftFile],
    );
    newOpenThreads[BlockIndex.rightFile] = Object.assign(
      {},
      this.openThreads[BlockIndex.rightFile],
    );
    return newOpenThreads;
  }
}
