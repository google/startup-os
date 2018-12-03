import { Location } from '@angular/common';
import { Injectable } from '@angular/core';

import { BranchInfo, Thread } from '@/core/proto';
import { LocalserverService, NotificationService } from '@/core/services';
import { BlockIndex } from '../code-changes/code-changes.interface';
import { StateService } from './state.service';

// Bunch of methods related to commits
@Injectable()
export class CommitService {
  constructor(
    private location: Location,
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
    private stateService: StateService,
  ) { }

  getCommitIndex(commitId: string): number {
    return this.stateService.commitIdList.indexOf(commitId);
  }

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

    // Switch left and right commits, if left commit id is newer than right one.
    // This cannot happen using the UI, but can by editing the url.
    const leftIndex: number = this.getCommitIndex(this.stateService.leftCommitId);
    const rightIndex: number = this.getCommitIndex(this.stateService.rightCommitId);
    if (leftIndex >= rightIndex && leftIndex !== -1 && rightIndex !== -1) {
      [this.stateService.leftCommitId, this.stateService.rightCommitId] =
      [this.stateService.rightCommitId, this.stateService.leftCommitId];
      this.updateUrl();
    }
  }

  // Get block index (0 or 1) depends on commit id
  getBlockIndex(thread: Thread): number {
    for (const blockIndex of [BlockIndex.leftFile, BlockIndex.rightFile]) {
      const commitId: string = this.stateService.getCommitIdByBlockIndex(blockIndex);
      if (commitId === thread.getCommitId()) {
        return blockIndex;
      }
    }
  }

  // Updates commit ids in query param
  updateUrl(): void {
    const paramList: string[][] = [
      ['left', this.stateService.leftCommitId],
      ['right', this.stateService.rightCommitId],
    ];
    const queryParams: string = paramList
      .filter(commit => commit[1])
      .map(commit => commit.join('='))
      .join('&');
    const currentState: string = [
      'diff',
      this.stateService.diffId,
      this.stateService.file.getFilenameWithRepo(),
    ].join('/');
    this.location.replaceState(currentState, queryParams);
  }

  // Add commit to commit list, if the commit isn't present already in the list.
  private addCommitId(commitId: string): void {
    if (this.stateService.commitIdList.indexOf(commitId) === -1) {
      this.stateService.commitIdList.push(commitId);
    }
  }
}
