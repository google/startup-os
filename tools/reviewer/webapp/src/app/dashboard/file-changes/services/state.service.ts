import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Diff,
  File,
  TextDiff,
  Thread,
} from '@/core/proto';
import { BlockIndex } from '../code-changes';

// The method keeps state of file-changes
@Injectable()
export class StateService {
  isLoading: boolean;
  diff: Diff;
  localThreads: Thread[];
  branchInfo: BranchInfo;
  textDiff: TextDiff;
  diffId: string;
  language: string;
  file = new File();
  isCommitFound: boolean;
  leftCommitId: string;
  rightCommitId: string;
  // TODO: replace commit list to file list to be able to get "action" and other file fields
  commitIdList: string[] = [];
  onloadSubscription = new Subscription();
  changesSubscription = new Subscription();

  createFile(): File {
    const file: File = new File();
    file.setFilename(this.file.getFilename());
    file.setRepoId(this.branchInfo.getRepoId());
    file.setWorkspace(this.diff.getWorkspace());

    return file;
  }

  getCommitIdByBlockIndex(blockIndex: BlockIndex): string {
    return [this.leftCommitId, this.rightCommitId][blockIndex];
  }
}
