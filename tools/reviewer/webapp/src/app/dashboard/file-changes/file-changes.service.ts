import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Comment,
  Diff,
  File,
  TextDiff,
  Thread,
} from '@/shared/proto';
import {
  FirebaseService,
  LocalserverService,
  NotificationService,
} from '@/shared/services';

@Injectable()
export class FileChangesService {
  isLoading: boolean = true;
  diff: Diff;
  file: File = new File();
  localThreads: Thread[];
  branchInfo: BranchInfo;
  textDiff: TextDiff;
  commitId: string[];
  firebaseSubscription = new Subscription();

  constructor(
    private firebaseService: FirebaseService,
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
  ) { }

  startLoading(filename: string, diffId: string): void {
    this.file.setFilenameWithRepo(filename);
    this.getDiff(diffId);
  }

  // Download diff from firebase
  getDiff(diffId: string): void {
    this.firebaseSubscription = this.firebaseService
      .getDiff(diffId)
      .subscribe(diff => {
        this.diff = diff;
        this.localThreads = this.diff
          .getThreadList()
          .filter(thread =>
            thread.getFile().getFilenameWithRepo() ===
            this.file.getFilenameWithRepo(),
          );

        this.getBranchInfo();
      });
  }

  // Get branchInfo from localserver
  getBranchInfo(): void {
    this.localserverService
      .getBranchInfo(this.diff.getId(), this.diff.getWorkspace())
      .subscribe(branchInfoList => {
        const { branchInfo, file } = this.getFileData(
          this.file.getFilenameWithRepo(),
          branchInfoList,
        );
        this.branchInfo = branchInfo;
        this.file = file;
        this.getFileChanges(this.file);
      });
  }

  // Get files from localserver
  getFileChanges(currentFile: File): void {
    // Left commit id - commit before the changes.
    const leftCommitId: string = this.branchInfo
      .getCommitList()[0]
      .getId();

    const leftFile: File = new File();
    leftFile.setCommitId(leftCommitId);
    leftFile.setFilename(currentFile.getFilename());
    leftFile.setRepoId(this.branchInfo.getRepoId());
    leftFile.setWorkspace(this.diff.getWorkspace());
    const rightFile: File = currentFile;

    this.saveCommitId(leftFile, rightFile);

    this.localserverService
      .getFileChanges(leftFile, rightFile)
      .subscribe(textDiffResponse => {
        this.textDiff = textDiffResponse.getTextDiff();
        this.isLoading = false;
      });
  }

  saveCommitId(leftFile: File, rightFile: File) {
    this.commitId = [leftFile.getCommitId(), rightFile.getCommitId()];
  }

  // Send diff with new comment to firebase
  addComment(lineNumber: number, comments: Comment[]): void {
    if (!this.file.getCommitId()) {
      // TODO: Add more UX behavior here
      // For example:
      // Remove button 'add comment' if it's an uncommitted file.
      // Or open uncommitted files in a special readonly mode.
      // etc
      this.notificationService
        .error('Comment cannot be added to an uncommitted file');
      return;
    }

    if (comments.length === 1) {
      // Create new thread
      const newThread: Thread = this.createNewThread(lineNumber, comments);
      this.diff.addThread(newThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Comment is saved in firebase');
    });
  }

  private createNewThread(lineNumber: number, comments: Comment[]): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);
    newThread.setRepoId(this.branchInfo.getRepoId());
    // TODO: add ability to add comment to left file (to old commits)
    newThread.setCommitId(this.file.getCommitId());
    newThread.setFile(this.file);

    return newThread;
  }

  deleteComment(isDeleteThread: boolean): void {
    if (isDeleteThread) {
      // Delete all threads without comments.
      const threads: Thread[] = this.diff.getThreadList();
      threads.forEach((thread, threadIndex) => {
        if (thread.getCommentList().length === 0) {
          threads.splice(threadIndex, 1);
        }
      });
      this.diff.setThreadList(threads);
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Comment is deleted');
    });
  }

  resolveThread(isDone: boolean): void {
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const threadStatus: string = isDone ? 'resolved' : 'unresolved';
      this.notificationService.success('Thread is ' + threadStatus);
    });
  }

  // Get block index (0 or 1) depends on commit id
  getBlockIndex(thread: Thread): number {
    for (let i = 0; i <= 1; i++) {
      const commitId = this.commitId[i];
      if (commitId === thread.getCommitId()) {
        return i;
      }
    }
    throw new Error('Undefined commit id');
  }

  // Get file and branchInfo from branchInfo list
  getFileData(filenameWithRepo: string, branchInfoList: BranchInfo[]): {
    file: File;
    branchInfo: BranchInfo;
  } {
    // Compare the filename with each filename of each repo
    for (const branchInfo of branchInfoList) {
      const files: File[] = this.localserverService
        .getFilesFromBranchInfo(branchInfo);
      for (const file of files) {
        if (filenameWithRepo === file.getFilenameWithRepo()) {
          // File found
          return {
            file: file,
            branchInfo: branchInfo,
          };
        }
      }
    }
    throw new Error('File not found');
  }

  // Get langulage from filename. Example:
  // filename.js -> javascript
  getLanguage(filename: string): string {
    const extensionRegExp: RegExp = /(?:\.([^.]+))?$/;
    const extension: string = extensionRegExp.exec(filename)[1];

    switch (extension) {
      case 'js': return 'javascript';
      case 'ts': return 'typescript';
      case 'java': return 'java';
      case 'proto': return 'protobuf';
      case 'md': return 'markdown';
      case 'json': return 'json';
      case 'css': return 'css';
      case 'scss': return 'scss';
      case 'html': return 'html';
      case 'sh': return 'bash';
      case 'xml': return 'xml';
      case 'py': return 'python';

      default: return 'clean';
    }

    // All supported languages:
    // https://github.com/highlightjs/highlight.js/tree/master/src/languages
  }

  destroy(): void {
    this.firebaseSubscription.unsubscribe();
  }
}
