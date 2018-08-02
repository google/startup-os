import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
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
  DifferenceService,
  FirebaseService,
  LocalserverService,
  NotificationService,
} from '@/shared/services';
import { FileChangesService } from './file-changes.service';

// The component implements a diff
@Component({
  selector: 'file-changes',
  templateUrl: './file-changes.component.html',
  styleUrls: ['./file-changes.component.scss'],
})
export class FileChangesComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  textDiff: TextDiff;
  changes: number[];
  localThreads: Thread[];
  diff: Diff;
  addCommentSubscription: Subscription;
  deleteCommentSubscription: Subscription;
  file: File = new File();
  branchInfo: BranchInfo;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private fileChangesService: FileChangesService,
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
  ) {
    this.addCommentSubscription = this.fileChangesService
      .addCommentChanges.subscribe(param => {
        this.addComment(param.lineNumber, param.comments);
      });

    this.deleteCommentSubscription = this.fileChangesService.
      deleteCommentChanges.subscribe(isDeleteThread => {
        this.deleteComment(isDeleteThread);
      });
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot: ActivatedRouteSnapshot = this.activatedRoute.snapshot;
    const filename: string = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    this.file.setFilename(filename);

    this.getDiff(urlSnapshot.url[0].path);
  }

  getDiff(diffId: string): void {
    this.firebaseService.getDiff(diffId).subscribe(diff => {
      this.diff = diff;
      this.localThreads = this.diff
        .getThreadList()
        .filter(v => v.getFile().getFilename() === this.file.getFilename());

      this.getBranchInfo();
    });
  }

  getBranchInfo(): void {
    this.localserverService
      .getBranchInfo(this.diff.getId(), this.diff.getWorkspace())
      .subscribe(branchInfo => {
        this.branchInfo = branchInfo;
        this.file = this.getFile(this.file.getFilename(), this.branchInfo);
        this.getFileChanges(this.file);
      });
  }

  // Get file from branchInfo by the filename
  getFile(filename: string, branchInfo: BranchInfo): File {
    const files: File[] = this.localserverService
      .getFilesFromBranchInfo(branchInfo);
    for (const file of files) {
      if (filename === file.getFilename()) {
        return file;
      }
    }
  }

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

    this.localserverService
      .getFileChanges(leftFile, rightFile)
      .subscribe(textDiffResponse => {
        this.textDiff = textDiffResponse.getTextDiff();

        // TODO: use changes from localserver instead
        this.changes = this.differenceService.compare(
          this.textDiff.getLeftFileContents(),
          this.textDiff.getRightFileContents(),
        );

        this.isLoading = false;
      });
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

  createNewThread(lineNumber: number, comments: Comment[]): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);
    newThread.setRepoId(this.branchInfo.getRepoId());
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

  ngOnDestroy() {
    this.addCommentSubscription.unsubscribe();
  }
}
