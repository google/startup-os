import { Injectable } from '@angular/core';

import { BranchInfo, Thread } from '@/shared/proto';
import { LocalserverService, NotificationService } from '@/shared/services';
import { StateService } from './state.service';
import { ThreadService } from './thread.service';

// Bunch of methods related to commits
@Injectable()
export class CommitService {
  constructor(
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
    private stateService: StateService,
    private threadService: ThreadService,
  ) { }

  getCurrentCommitIds(): void {
    // Commit id before the changed. Left default value.
    const headCommitId: string = this.stateService.branchInfo.getCommitList()[0].getId();
    // Commit id of last change with the file. Right default value.
    const lastCommitId: string = this.stateService.file.getCommitId();

    // Use default value, if custom commit id isn't set
    this.stateService.leftCommitId = this.stateService.leftCommitId || headCommitId;
    this.stateService.rightCommitId = this.stateService.rightCommitId || lastCommitId;

    // Get list of threads of current commits state
    this.stateService.localThreads = this.threadService.getLocalThreads(
      this.stateService.diff,
      this.stateService.file,
      this.stateService.leftCommitId,
      this.stateService.rightCommitId,
    );
  }

  // To don't show UI of changes, if commit don't exist
  contentCheck(): void {
    // If we get empty response, then commit doesn't exist
    if (
      !this.stateService.textDiff.getRightFileContents() &&
      !this.stateService.textDiff.getLeftFileContents()
    ) {
      this.notificationService.error('Commit not found');
      this.stateService.isCommitFound = false;
    } else {
      this.stateService.isCommitFound = true;
    }
  }

  // List of all commits related to current file
  createCommitList(filenameWithRepo: string, branchInfo: BranchInfo): void {
    this.stateService.commitIdList = this.localserverService.getCommitIdList(
      filenameWithRepo,
      branchInfo,
    );

    this.addCommitId(this.stateService.leftCommitId);
    this.addCommitId(this.stateService.rightCommitId);
  }

  // Get block index (0 or 1) depends on commit id
  getBlockIndex(thread: Thread): number {
    const commitBlockList: string[] = this.stateService.getCommitBlockList();
    for (let i = 0; i <= 1; i++) {
      const commitId: string = commitBlockList[i];
      if (commitId === thread.getCommitId()) {
        return i;
      }
    }
  }

  // Add commit to commit list,
  // if file exists and doesn't present already in the list.
  private addCommitId(commitId: string): void {
    if (
      commitId &&
      this.stateService.commitIdList.indexOf(commitId) === -1
    ) {
      this.stateService.commitIdList.push(commitId);
    }
  }
}
