import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

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
    private router: Router,
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
    // '/diff/33/path/to/file.java?left=abc' -> [url, 33, 'path/to/file.java', param]
    const url: RegExpMatchArray = this.router.url.match(/\/diff\/([\d]+)\/([\w\d\.\/-]+)(\?.+?)?/);
    this.stateService.diffId = url[1];
    const filename: string = url[2];

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
