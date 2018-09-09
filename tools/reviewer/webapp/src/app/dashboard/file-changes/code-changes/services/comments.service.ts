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
      lineThread.comments = thread;
    }
    changesLine.commentsLine.blocks[blockIndex].threads.push(lineThread);
  }

  // Remove all threads without comments from the line
  clearThreads(changesLine: ChangesLine, blockIndex: number): void {
    const threads: LineThread[] = changesLine.blocks[blockIndex].threads;
    changesLine.blocks[blockIndex].threads = threads.filter(thread => {
      return thread.comments.getCommentList().length !== 0;
    });
  }

  // Remove all threads with comments from the line
  closeThreads(changesLine: ChangesLine, blockIndex: number): void {
    const threads: LineThread[] = changesLine.blocks[blockIndex].threads;
    changesLine.blocks[blockIndex].threads = threads.filter(thread => {
      return thread.comments.getCommentList().length === 0;
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
