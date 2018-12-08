import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { UserService } from '@/core/services';
import {
  CommitService,
  ExtensionService,
  LoadService,
  StateService,
  ThreadService,
} from './services';

const headerTopStart: number = 98;

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
  ],
})
export class FileChangesComponent implements OnInit, OnDestroy {
  isHeaderFixed: boolean = false;
  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    public stateService: StateService,
    private loadService: LoadService,
    private extensionService: ExtensionService,
    public commitService: CommitService,
    public userService: UserService,
  ) {
    this.stateService.isLoading = true;
    this.stateService.isCommitFound = true;
    document.body.style.width = 'auto';
    document.body.style.minWidth = '100%';

    document.onscroll = () => {
      const scrollTop: number = document.documentElement.scrollTop || document.body.scrollTop;
      this.isHeaderFixed = scrollTop >= headerTopStart;
    };
  }

  ngOnInit() {
    this.parseUrlParam();
  }

  // Gets parameters from url
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

  getAuthor(): string {
    return this.userService.getUsername(this.stateService.diff.getAuthor().getEmail());
  }

  ngOnDestroy() {
    delete this.stateService.rightCommitId;
    delete this.stateService.leftCommitId;
    document.body.style.width = '100%';
    document.body.style.minWidth = 'auto';
    document.onscroll = null;
    this.loadService.destroy();
  }
}
