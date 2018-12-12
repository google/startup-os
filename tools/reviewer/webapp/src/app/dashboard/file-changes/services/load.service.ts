import { Injectable } from '@angular/core';

import { Diff, File } from '@/core/proto';
import {
  ExceptionService,
  FirebaseStateService,
  LocalserverService,
} from '@/core/services';
import { CommitService } from './commit.service';
import { StateService } from './state.service';
import { ThreadService } from './thread.service';

// The service loads basic data, such as diff, branchInfo, and textDiff
@Injectable()
export class LoadService {
  constructor(
    private firebaseStateService: FirebaseStateService,
    private exceptionService: ExceptionService,
    private localserverService: LocalserverService,
    private stateService: StateService,
    private commitService: CommitService,
    private threadService: ThreadService,
  ) { }

  // Loads diff from firebase
  loadDiff(diffId: string): void {
    this.stateService.onloadSubscription = this.firebaseStateService
      .getDiff(diffId)
      .subscribe(diff => {
        this.setDiff(diff);
        this.subscribeOnChanges();
      });
  }

  // Each time when diff is changed in firebase, we receive new diff here.
  private subscribeOnChanges(): void {
    this.stateService.changesSubscription = this.firebaseStateService
      .diffChanges
      .subscribe(diff => {
        this.setDiff(diff);
      });
  }

  // When diff is received from firebase
  private setDiff(diff: Diff): void {
    if (diff === undefined) {
      this.exceptionService.diffNotFound();
      return;
    }
    this.stateService.diff = diff;

    this.loadFileDate();
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

    this.commitService.updateUrl();

    // Update changes
    this.destroy();
    this.loadDiff(this.stateService.diffId);
  }

  destroy(): void {
    this.stateService.onloadSubscription.unsubscribe();
    this.stateService.changesSubscription.unsubscribe();
  }
}
