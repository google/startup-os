import { Injectable } from '@angular/core';
import { randstr64 } from 'rndmjs';

import { Comment, Diff, File, Thread } from '@/core/proto';
import { DiffUpdateService, NotificationService } from '@/core/services';
import { BlockIndex } from '../code-changes';
import { StateService } from './state.service';

// Functions related to threads
@Injectable()
export class ThreadService {
  constructor(
    private notificationService: NotificationService,
    private diffUpdateService: DiffUpdateService,
    private stateService: StateService,
  ) { }

  private getLocalThreads(
    diff: Diff,
    file: File,
    leftCommitId: string,
    rightCommitId: string,
  ): Thread[] {
    return diff.getCodeThreadList()
      .filter(thread => {
        const filenamesAreEqual: boolean =
          thread.getFile().getFilenameWithRepo() ===
          file.getFilenameWithRepo();

        const isCurrentCommitId: boolean =
          thread.getCommitId() === leftCommitId ||
          thread.getCommitId() === rightCommitId;

        return filenamesAreEqual && isCurrentCommitId;
      });
  }

  createLocalThreads(): void {
    // Get list of threads of current commits state
    this.stateService.localThreads = this.getLocalThreads(
      this.stateService.diff,
      this.stateService.file,
      this.stateService.leftCommitId,
      this.stateService.rightCommitId,
    );
  }

  // Send diff with new comment to firebase
  addComment(
    lineNumber: number,
    comment: Comment,
    thread: Thread,
    blockIndex: BlockIndex,
  ): void {
    if (!this.stateService.getCommitIdByBlockIndex(blockIndex)) {
      // TODO: Add more UX behavior here
      // For example:
      // Remove button 'add comment' if it's an uncommitted file.
      // Or open uncommitted files in a special readonly mode.
      // etc
      this.notificationService
        .error('Comment cannot be added to an uncommitted file');

      throw new Error('Comment cannot be added to an uncommitted file');
    }

    thread.addComment(comment);
    if (thread.getCommentList().length === 1) {
      // Create new thread
      const newThread: Thread = this.createNewThread(
        lineNumber,
        thread.getCommentList(),
        this.stateService.getCommitIdByBlockIndex(blockIndex),
      );
      this.stateService.diff.addCodeThread(newThread);
    }

    this.diffUpdateService.saveComment(this.stateService.diff);
  }

  private createNewThread(
    lineNumber: number,
    comments: Comment[],
    commitId: string,
  ): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);
    newThread.setRepoId(this.stateService.branchInfo.getRepoId());
    newThread.setCommitId(commitId);
    newThread.setFile(this.stateService.file);
    newThread.setType(Thread.Type.CODE);
    newThread.setId(randstr64(6));

    return newThread;
  }

  deleteComment(isDeleteThread: boolean): void {
    this.diffUpdateService.deleteComment(this.stateService.diff, isDeleteThread);
  }

  resolveThread(isDone: boolean): void {
    this.diffUpdateService.resolveThread(this.stateService.diff, isDone);
  }
}
