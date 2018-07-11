// NOTICE: it's actually not a diff, it's a two files changes
// TODO: rename the component and linked files
// e.g. FileChangeComponent

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  file = new File();
  branchInfo: BranchInfo;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService,
    private localserverService: LocalserverService,
  ) {
    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        this.addComment(param.lineNumber, param.comments);
      },
    );
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    const filename = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    this.file.setFilename(filename);

    this.getDiff(urlSnapshot.url[0].path);
  }

  getDiff(diffId: string): void {
    this.firebaseService.getDiff(diffId).subscribe(diff => {
      this.diff = diff;
      this.file.setWorkspace(this.diff.getWorkspace());
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
        this.file.setRepoId(this.branchInfo.getRepoId());
        this.setCommitId(this.file, this.branchInfo);
        this.getFileChanges(this.file);
      });
  }

  // Get id of commit, where the file is present
  setCommitId(currentFile: File, branchInfo: BranchInfo): void {
    for (const commit of this.branchInfo.getCommitList()) {
      for (const file of commit.getFileList()) {
        if (currentFile.getFilename() === file.getFilename()) {
          currentFile.setCommitId(commit.getId());
        }
      }
    }
  }

  getFileChanges(file: File): void {
    // First commit in the list - commit before the changes.
    // Second commit in the list - the oldest commit in the diff.
    // ...
    // Last commit in the list - most recent commit in the diff.

    // Left commit id - first commit in the list.
    const leftCommitId: string = this.branchInfo
      .getCommitList()[0]
      .getId();
    // Right commit id - last commit in the list.
    const rightCommitId: string = this.branchInfo
      .getCommitList()[this.branchInfo.getCommitList().length - 1]
      .getId();

    const leftFile = new File();
    leftFile.setFilename(file.getFilename());
    leftFile.setRepoId(this.branchInfo.getRepoId());
    leftFile.setCommitId(leftCommitId);
    const rightFile = new File();
    rightFile.setFilename(file.getFilename());
    rightFile.setRepoId(this.branchInfo.getRepoId());
    rightFile.setCommitId(rightCommitId);

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
    if (comments.length === 1) {
      // Create new thread
      const newThread: Thread = this.createNewThread(lineNumber, comments);
      this.diff.addThread(newThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe();
  }

  createNewThread(lineNumber: number, comments: Comment[]): Thread {
    const newThread = new Thread();
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
