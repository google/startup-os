import { Location } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Diff,
  File,
  TextDiff,
  Thread,
} from '@/core/proto';
import {
  ExceptionService,
  FirebaseStateService,
  TextDiffService,
  UserService,
} from '@/core/services';

const headerTopStart: number = 98;

// The component implements file-changes page.
// Frame for code-changes.
@Component({
  selector: 'file-changes',
  templateUrl: './file-changes.component.html',
  styleUrls: ['./file-changes.component.scss'],
})
export class FileChangesComponent implements OnInit, OnDestroy {
  isHeaderFixed: boolean = false;
  isLoading: boolean;
  diff: Diff;
  localThreads: Thread[];
  branchInfo: BranchInfo;
  textDiff: TextDiff;
  diffId: string;
  filenameWithRepo: string;
  leftFile = new File();
  rightFile = new File();
  filesSortedByCommits: File[] = [];
  onloadSubscription = new Subscription();
  changesSubscription = new Subscription();

  constructor(
    private firebaseStateService: FirebaseStateService,
    private exceptionService: ExceptionService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private location: Location,
    private textDiffService: TextDiffService,
    private userService: UserService,
  ) {
    this.isLoading = true;
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
  private parseUrlParam(): void {
    // '/diff/33/path/to/file.java?left=abc' -> [url, '33', 'path/to/file.java', param]
    const url: RegExpMatchArray = this.router.url.match(/\/diff\/([\d]+)\/([\w\d\.\/-]+)(\?.+?)?/);
    if (!url) {
      // User is trying to open '/diff'
      this.router.navigate(['/']);
      return;
    }
    this.diffId = url[1];
    const filename: string = url[2];

    this.filenameWithRepo = filename;

    // Get left and right commit ids from url if they present
    this.activatedRoute.queryParams.subscribe(params => {
      this.leftFile.setCommitId(params.left);
      this.rightFile.setCommitId(params.right);

      this.loadDiff(this.diffId);
    });
  }

  // Loads diff from firebase
  private loadDiff(diffId: string): void {
    this.onloadSubscription = this.firebaseStateService
      .getDiff(diffId)
      .subscribe(diff => {
        this.setDiff(diff);
        this.subscribeOnChanges();
      });
  }

  // Each time when diff is changed in firebase, we receive new diff here.
  private subscribeOnChanges(): void {
    this.changesSubscription = this.firebaseStateService
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
    this.diff = diff;

    // Load changes from local server
    this.textDiffService.load(
      this.diff,
      this.filenameWithRepo,
      this.leftFile.getCommitId(),
      this.rightFile.getCommitId(),
    ).subscribe(textDiffBundle => {
      this.branchInfo = textDiffBundle.branchInfo;
      this.leftFile = textDiffBundle.leftFile;
      this.rightFile = textDiffBundle.rightFile;
      this.filesSortedByCommits = textDiffBundle.filesSortedByCommits;
      this.textDiff = textDiffBundle.textDiff;
      this.localThreads = textDiffBundle.localThreads;

      this.isLoading = false;
    }, (error: Error) => {
      this.exceptionService.fileNotFound(this.diff.getId());
    });
  }

  // Converts file list to commit list
  getCommitIdList(filesSortedByCommits: File[]): string[] {
    const commitIdList: string[] = [];
    for (const file of filesSortedByCommits) {
      commitIdList.push(file.getCommitId());
    }
    return commitIdList;
  }

  getAuthor(): string {
    return this.userService.getUsername(this.diff.getAuthor().getEmail());
  }

  changeCommitId(param): void {
    this.isLoading = true;

    this.leftFile.setCommitId(param.leftCommitId);
    this.rightFile.setCommitId(param.rightCommitId);

    // Convert local variables to url with query params
    const paramList: string[][] = [
      ['left', this.leftFile.getCommitId()],
      ['right', this.rightFile.getCommitId()],
    ];
    const queryParams: string = paramList
      .filter(commit => commit[1])
      .map(commit => commit.join('='))
      .join('&');
    const currentState: string = [
      'diff',
      this.diffId,
      this.filenameWithRepo,
    ].join('/');

    // Update url
    this.location.replaceState(currentState, queryParams);

    // Update changes
    this.unsubscribe();
    this.loadDiff(this.diffId);
  }

  unsubscribe(): void {
    this.onloadSubscription.unsubscribe();
    this.changesSubscription.unsubscribe();
  }

  ngOnDestroy() {
    document.body.style.width = '100%';
    document.body.style.minWidth = 'auto';
    document.onscroll = null;
    this.unsubscribe();
  }
}
