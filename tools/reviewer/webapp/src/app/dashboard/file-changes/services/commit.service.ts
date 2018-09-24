import { Injectable } from '@angular/core';

import { BranchInfo, Thread } from '@/shared/proto';
import { LocalserverService, NotificationService } from '@/shared/services';
import { StateService } from './state.service';
import { BlockIndex } from '../code-changes/code-changes.interface';

// Bunch of methods related to commits
@Injectable()
export class CommitService {
  constructor(
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
    private stateService: StateService,
  ) { }

  createCurrentCommitIds(): void {
    // Left default value is commit id before the change.
    const headCommitId: string = this.stateService.branchInfo.getCommitList()[0].getId();
    // Right default value is commit id of last change with the file.
    const lastCommitId: string = this.stateService.file.getCommitId();

    // Use the default value, if custom commit id isn't set
    this.stateService.leftCommitId = this.stateService.leftCommitId || headCommitId;
    this.stateService.rightCommitId = this.stateService.rightCommitId || lastCommitId;
  }

  // To not show UI of changes, if commit doesn't exist
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
    for (let blockIndex of [BlockIndex.leftFile, BlockIndex.rightFile]) {
      const commitId: string = this.stateService.getCommitIdByBlockIndex(blockIndex);
      if (commitId === thread.getCommitId()) {
        return blockIndex;
      }
    }
  }

  // Add commit to commit list,
  // if file exists and isn't present already in the list.
  private addCommitId(commitId: string): void {
    if (
      commitId &&
      this.stateService.commitIdList.indexOf(commitId) === -1
    ) {
      this.stateService.commitIdList.push(commitId);
    }
  }
}
