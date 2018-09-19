import { Injectable } from '@angular/core';

import { Comment, Diff, File, Thread } from '@/shared/proto';
import { FirebaseService, NotificationService } from '@/shared/services';
import { StateService } from './state.service';

// Functions related to threads
@Injectable()
export class ThreadService {
  constructor(
    private notificationService: NotificationService,
    private firebaseService: FirebaseService,
    private stateService: StateService,
  ) { }

  getLocalThreads(
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

  // Send diff with new comment to firebase
  addComment(
    lineNumber: number,
    comment: Comment,
    blockIndex: number,
    isNewThread: boolean,
  ): boolean {
    const commitIds: string[] = this.stateService.getCommitBlockList();

    if (!commitIds[blockIndex]) {
      // TODO: Add more UX behavior here
      // For example:
      // Remove button 'add comment' if it's an uncommitted file.
      // Or open uncommitted files in a special readonly mode.
      // etc
      this.notificationService
        .error('Comment cannot be added to an uncommitted file');

      // Is error?
      return true;
    }

    if (isNewThread) {
      // Create new thread
      const newThread: Thread = this.createNewThread(
        lineNumber,
        comment,
        commitIds[blockIndex],
      );
      this.stateService.diff.addCodeThread(newThread);
    }

    this.firebaseService.updateDiff(this.stateService.diff).subscribe(() => {
      this.notificationService.success('Comment is saved in firebase');
    });
  }

  private createNewThread(
    lineNumber: number,
    comment: Comment,
    commitId: string,
  ): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.addComment(comment);
    newThread.setRepoId(this.stateService.branchInfo.getRepoId());
    newThread.setCommitId(commitId);
    newThread.setFile(this.stateService.file);
    newThread.setType(Thread.Type.CODE);

    return newThread;
  }

  deleteComment(isDeleteThread: boolean): void {
    if (isDeleteThread) {
      // Delete all threads without comments.
      const threads: Thread[] = this.stateService.diff.getCodeThreadList();
      threads.forEach((thread, threadIndex) => {
        if (thread.getCommentList().length === 0) {
          threads.splice(threadIndex, 1);
        }
      });
      this.stateService.diff.setCodeThreadList(threads);
    }

    this.firebaseService.updateDiff(this.stateService.diff).subscribe(() => {
      this.notificationService.success('Comment is deleted');
    });
  }

  resolveThread(isDone: boolean): void {
    this.firebaseService.updateDiff(this.stateService.diff).subscribe(() => {
      const threadStatus: string = isDone ? 'resolved' : 'unresolved';
      this.notificationService.success('Thread is ' + threadStatus);
    });
  }
}
