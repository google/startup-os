import { Injectable } from '@angular/core';

import { Thread } from '@/shared/proto';
import {
  ChangesLine,
  LineThread,
} from '../code-changes.interface';
import { LineService } from './line.service';

@Injectable()
export class CommentsService {
  // Line indexes of all open threads.
  openThreadsMap: { [id: number]: number }[];

  constructor(private lineService: LineService) {
    this.openThreadsMap = this.lineService.createSplitDictionary();
  }

  // Add a thread to the line
  addThread(changesLine: ChangesLine, blockIndex: number, thread?: Thread): void {
    const lineThread: LineThread = this.lineService.createLineThread();
    if (thread) {
      lineThread.thread = thread;
    }
    changesLine.commentsLine.blocks[blockIndex].lineThreads.push(lineThread);
  }

  // Remove all threads without comments from the line
  clearThreads(changesLine: ChangesLine, blockIndex: number): void {
    const threads: LineThread[] = changesLine.blocks[blockIndex].lineThreads;
    changesLine.blocks[blockIndex].lineThreads = threads.filter(lineThread => {
      return lineThread.thread.getCommentList().length !== 0;
    });
  }

  // Remove all threads with comments from the line
  closeThreads(changesLine: ChangesLine, blockIndex: number): void {
    const threads: LineThread[] = changesLine.blocks[blockIndex].lineThreads;
    changesLine.blocks[blockIndex].lineThreads = threads.filter(lineThread => {
      return lineThread.thread.getCommentList().length === 0;
    });
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
