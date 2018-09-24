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
import { ThreadService } from './thread.service';

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
    private threadService: ThreadService,
  ) { }

  // Download diff from firebase
  loadDiff(diffId: string): void {
    this.stateService.subscription = this.firebaseService
      .getDiff(diffId)
      .subscribe(diff => {
        if (diff === undefined) {
          this.exceptionService.diffNotFound();
          return;
        }
        this.stateService.diff = diff;

        this.loadFileDate();
      });
  }

  // Get branchInfo and file from localserver
  private loadFileDate(): void {
    // Get branchInfoList from localserver
    this.localserverService
      .getBranchInfoList(
        this.stateService.diff.getId(),
        this.stateService.diff.getWorkspace(),
      )
      .subscribe(branchInfoList => {
        // Get file and branchInfo by branchInfoList
        try {
          const { branchInfo, file } = this.localserverService.getFileData(
            this.stateService.file.getFilenameWithRepo(),
            branchInfoList,
          );
          this.stateService.branchInfo = branchInfo;
          this.stateService.file = file;

          // Create commits
          this.commitService.createCurrentCommitIds();
          this.commitService.createCommitList(
            this.stateService.file.getFilenameWithRepo(),
            this.stateService.branchInfo,
          );

          // Create local threads
          this.threadService.createLocalThreads();

          this.loadFileChanges();
        } catch (e) {
          this.exceptionService.fileNotFound(this.stateService.diff.getId());
        }
      });
  }

  // Get file content and changes from localserver
  private loadFileChanges(): void {
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
      ['left', this.stateService.leftCommitId],
      ['right', this.stateService.rightCommitId],
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
    this.loadDiff(this.stateService.diffId);
  }

  destroy(): void {
    this.stateService.subscription.unsubscribe();
  }
}
