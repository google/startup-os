import { Injectable } from '@angular/core';

import { Thread } from '@/core/proto';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
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

  // Add a thread to the line
  addThread(changesLine: ChangesLine, blockIndex: BlockIndex, thread: Thread): void {
    changesLine.commentsLine.blocks[blockIndex].threads.push(thread);
  }

  // Add empty thread to the line
  addEmptyThread(changesLine: ChangesLine, blockIndex: BlockIndex): Thread {
    const thread: Thread = new Thread();
    thread.setLineNumber(changesLine.blocks[blockIndex].lineNumber);
    this.addThread(changesLine, blockIndex, thread);
    return thread;
  }

  // Remove all threads with comments from the line
  closeThreads(changesLine: ChangesLine, blockIndex: BlockIndex): void {
    const blockLine: BlockLine = changesLine.blocks[blockIndex];
    const threads: Thread[] = blockLine.threads;
    blockLine.threads = threads.filter(thread => {
      return thread.getCommentList().length === 0;
    });

    if (blockLine.threads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex);
    }
  }

  // Remove thread by thread index
  closeThread(blockLine: BlockLine, threadIndex: number, blockIndex: BlockIndex): void {
    blockLine.threads.splice(threadIndex, 1);

    if (blockLine.threads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex);
    }
  }

  // Add to open threads map
  saveAsOpen(
    lineNumber: number,
    lineIndex: number,
    blockIndex: BlockIndex,
  ): void {
    this.openThreadsMap[blockIndex][lineNumber] = lineIndex;
  }

  // Remove from open threads map
  saveAsClosed(lineNumber: number, blockIndex: BlockIndex): void {
    delete this.openThreadsMap[blockIndex][lineNumber];
  }
}
