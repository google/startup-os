// NOTICE: it's actually not a diff, it's a two files changes
// TODO: rename the component and linked files
// e.g. FileChangeComponent

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Comment,
  Diff,
  File,
  TextDiffResponse,
  Thread,
} from '@/shared';
import {
  DifferenceService,
  FirebaseService,
  LocalserverService,
  NotificationService,
} from '@/shared/services';
import { DiffService } from './diff.service';

// The component implements a diff
@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss'],
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  textDiffResponse: TextDiffResponse;
  changes: number[];
  localThreads: Thread[];
  diff: Diff;
  newCommentSubscription: Subscription;
  file: File = new File();
  branchInfo: BranchInfo;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService,
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
  ) {
    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        this.addComment(param.lineNumber, param.comments);
      },
    );
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
        this.textDiffResponse = textDiffResponse;

        // TODO: use textDiffResponse.getChangesList() instead
        this.changes = this.differenceService.compare(
          textDiffResponse.getLeftFileContents(),
          textDiffResponse.getRightFileContents(),
        );

        this.isLoading = false;
      });
  }

  // Send diff with new comment to firebase
  addComment(lineNumber: number, comments: Comment[]): void {
    if (!this.file.getCommitId()) {
      // TODO: Add more UX behavior here
      this.notificationService
        .error('Comment cannot be added to a uncommitted file');
      return;
    }

    if (comments.length === 1) {
      // Create new thread
      const newThread: Thread = this.createNewThread(lineNumber, comments);
      this.diff.addThread(newThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe();
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

  ngOnDestroy() {
    this.newCommentSubscription.unsubscribe();
  }
}
