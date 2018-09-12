import { Injectable } from '@angular/core';

import {
  BlockLine,
  ChangesLine,
  LineThread,
} from '../code-changes.interface';
import { LineService } from './line.service';

// Functions related to comments
@Injectable()
export class CommentsService {
  // Line indexes of all open threads.
  openThreadsMap: { [id: number]: number }[];

  constructor(private lineService: LineService) {
    this.openThreadsMap = this.lineService.createSplitDictionary();
  }

  // Add empty thread to the line
  addEmptyThread(changesLine: ChangesLine, blockIndex: number): LineThread {
    const lineThread: LineThread = this.lineService.createLineThread();
    changesLine.commentsLine.blocks[blockIndex].lineThreads.push(lineThread);
    return lineThread;
  }

  // Remove all threads with comments from the line
  closeThreads(changesLine: ChangesLine, blockIndex: number): void {
    const blockLine: BlockLine = changesLine.blocks[blockIndex];
    const threads: LineThread[] = blockLine.lineThreads;
    blockLine.lineThreads = threads.filter(lineThread => {
      return lineThread.thread.getCommentList().length === 0;
    });

    if (blockLine.lineThreads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex);
    }
  }

  // Remove thread by thread index
  closeThread(blockLine: BlockLine, threadIndex: number, blockIndex: number): void {
    blockLine.lineThreads.splice(threadIndex, 1);

    if (blockLine.lineThreads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex);
    }
  }

  // Add to open threads map
  saveAsOpen(
    lineNumber: number,
    lineIndex: number,
    blockIndex: number,
  ): void {
    this.openThreadsMap[blockIndex][lineNumber] = lineIndex;
  }

  // Remove from open threads map
  saveAsClosed(lineNumber: number, blockIndex: number): void {
    delete this.openThreadsMap[blockIndex][lineNumber];
  }
}
