import { Location } from '@angular/common';
import { Injectable } from '@angular/core';

import { File } from '@/shared/proto';
import {
  ExceptionService,
  FirebaseService,
  LocalserverService,
} from '@/shared/services';
import { CommitService } from './commit.service';
import { StateService } from './state.service';

// The service loads basic data, such as diff, branchInfo, and textDiff
@Injectable()
export class LoadService {
  constructor(
    private location: Location,
    private firebaseService: FirebaseService,
    private exceptionService: ExceptionService,
    private localserverService: LocalserverService,
    private stateService: StateService,
    private commitService: CommitService,
  ) { }

  // Download diff from firebase
  getDiff(diffId: string): void {
    this.stateService.subscription = this.firebaseService
      .getDiff(diffId)
      .subscribe(diff => {
        if (diff === undefined) {
          this.exceptionService.diffNotFound();
          return;
        }
        this.stateService.diff = diff;

        this.getBranchInfo();
      });
  }

  // Get branchInfo from localserver
  private getBranchInfo(): void {
    this.localserverService
      .getBranchInfo(
        this.stateService.diff.getId(),
        this.stateService.diff.getWorkspace(),
      )
      .subscribe(branchInfoList => {
        // Get file and branchInfo
        this.localserverService.getFileData(
          this.stateService.file.getFilenameWithRepo(),
          branchInfoList,
        ).subscribe(fileData => {
          this.stateService.branchInfo = fileData.branchInfo;
          this.stateService.file = fileData.file;

          // Create list with all current commit ids
          this.commitService.createCommitList(
            this.stateService.file.getFilenameWithRepo(),
            this.stateService.branchInfo,
          );

          this.commitService.getCurrentCommitIds();
          this.getFileChanges();
        }, () => {
          this.exceptionService.fileNotFound(this.stateService.diff.getId());
        });
      });
  }

  // Get file content and changes from localserver
  private getFileChanges(): void {
    // Create left and right files for request
    const leftFile: File = this.stateService.createFile();
    leftFile.setCommitId(this.stateService.leftCommitId);

    const rightFile: File = this.stateService.createFile();
    rightFile.setCommitId(this.stateService.rightCommitId);
    rightFile.setAction(this.stateService.file.getAction());

    this.localserverService
      .getFileChanges(leftFile, rightFile)
      .subscribe(textDiffResponse => {
        this.stateService.textDiff = textDiffResponse.getTextDiff();
        this.commitService.contentCheck();

        // We're ready to start
        this.stateService.isLoading = false;
      });
  }

  changeCommitId(): void {
    this.stateService.isLoading = true;

    // Update url
    const paramList: string[][] = [
      ['left_commit_id', this.stateService.leftCommitId],
      ['right_commit_id', this.stateService.rightCommitId],
    ];
    const queryParams: string = paramList
      .map(commit => commit.join('='))
      .join('&');
    const currentState: string = [
      'diff',
      this.stateService.diffId,
      this.stateService.file.getFilenameWithRepo(),
    ].join('/');
    this.location.replaceState(currentState, queryParams);

    // Update changes
    this.destroy();
    this.getDiff(this.stateService.diffId);
  }

  destroy(): void {
    this.stateService.subscription.unsubscribe();
  }
}
