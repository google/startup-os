import { Injectable } from '@angular/core';
import { randstr64 } from 'rndmjs';

import { BranchInfo, Comment, File, Thread } from '@/core/proto';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
  Dictionary,
} from '../code-changes.interface';

// Functions related to comments
@Injectable()
export class CommentsService {
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
  closeThreads(
    changesLine: ChangesLine,
    blockIndex: BlockIndex,
    openThreadsMap: Dictionary[],
  ): void {
    const blockLine: BlockLine = changesLine.blocks[blockIndex];
    const threads: Thread[] = blockLine.threads;
    blockLine.threads = threads.filter((thread: Thread) => {
      return thread.getCommentList().length === 0;
    });

    if (blockLine.threads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex, openThreadsMap);
    }
  }

  // Remove thread by thread index
  closeThread(
    blockLine: BlockLine,
    threadIndex: number,
    blockIndex: BlockIndex,
    openThreadsMap: Dictionary[],
  ): void {
    blockLine.threads.splice(threadIndex, 1);

    if (blockLine.threads.length === 0) {
      this.saveAsClosed(blockLine.lineNumber, blockIndex, openThreadsMap);
    }
  }

  // Add to open threads map
  saveAsOpen(
    lineNumber: number,
    lineIndex: number,
    blockIndex: BlockIndex,
    openThreadsMap: Dictionary[],
  ): void {
    openThreadsMap[blockIndex][lineNumber] = lineIndex;
  }

  // Remove from open threads map
  saveAsClosed(
    lineNumber: number,
    blockIndex: BlockIndex,
    openThreadsMap: Dictionary[],
  ): void {
    delete openThreadsMap[blockIndex][lineNumber];
  }

  createNewThread(
    lineNumber: number,
    comments: Comment[],
    file: File,
    branchInfo: BranchInfo,
  ): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);
    newThread.setRepoId(branchInfo.getRepoId());
    newThread.setCommitId(file.getCommitId());
    newThread.setFile(file);
    newThread.setType(Thread.Type.CODE);
    newThread.setId(randstr64(6));

    return newThread;
  }
}
