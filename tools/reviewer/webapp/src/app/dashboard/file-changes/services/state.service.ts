import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Diff,
  File,
  TextDiff,
  Thread,
} from '@/shared/proto';

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
  commitIdList: string[] = [];
  subscription = new Subscription();

  createFile(): File {
    const file: File = new File();
    file.setFilename(this.file.getFilename());
    file.setRepoId(this.branchInfo.getRepoId());
    file.setWorkspace(this.diff.getWorkspace());

    return file;
  }

  getCommitBlockList(): string[] {
    return [this.leftCommitId, this.rightCommitId];
  }
}
