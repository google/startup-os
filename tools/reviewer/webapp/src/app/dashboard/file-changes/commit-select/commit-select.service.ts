import { Injectable } from '@angular/core';

import { LoadService, StateService } from '../services';

@Injectable()
export class CommitSelectService {
  constructor(
    private stateService: StateService,
    private loadService: LoadService,
  ) { }

  leftSelectChanged(commitId: string): void {
    if (!this.stateService.isLoading) {
      this.stateService.leftCommitId = commitId;
      this.loadService.changeCommitId();
    }
  }

  rightSelectChanged(commitId: string): void {
    if (!this.stateService.isLoading) {
      this.stateService.rightCommitId = commitId;
      this.loadService.changeCommitId();
    }
  }
}
