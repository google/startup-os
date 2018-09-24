import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { CommitSelectService } from './commit-select';
import {
  CommitService,
  ExtensionService,
  LoadService,
  StateService,
  ThreadService,
} from './services';

// The component implements file-changes page.
// Frame for code-changes.
@Component({
  selector: 'file-changes',
  templateUrl: './file-changes.component.html',
  styleUrls: ['./file-changes.component.scss'],
  providers: [
    LoadService,
    ExtensionService,
    ThreadService,
    CommitService,
    CommitSelectService,
  ],
})
export class FileChangesComponent implements OnInit, OnDestroy {
  constructor(
    private activatedRoute: ActivatedRoute,
    public stateService: StateService,
    private loadService: LoadService,
    private extensionService: ExtensionService,
    public commitService: CommitService,
    public commitSelectService: CommitSelectService,
  ) {
    this.stateService.isLoading = true;
    this.stateService.isCommitFound = true;
  }

  ngOnInit() {
    this.parseUrlParam();
  }

  // Get parameters from url
  parseUrlParam(): void {
    this.stateService.diffId = this.activatedRoute.snapshot.url[0].path;
    const filename: string = this.activatedRoute.snapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    this.stateService.file.setFilenameWithRepo(filename);

    this.stateService.language = this.extensionService.getLanguage(filename);

    // Get left and right commit ids from url if they present
    this.activatedRoute.queryParams.subscribe(params => {
      this.stateService.leftCommitId = params.left;
      this.stateService.rightCommitId = params.right;

      // Load changes from local server
      this.loadService.loadDiff(this.stateService.diffId);
    });
  }

  ngOnDestroy() {
    delete this.stateService.rightCommitId;
    delete this.stateService.leftCommitId;
    this.loadService.destroy();
  }
}
