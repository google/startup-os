import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { Diff } from '@/core/proto';
import {
  CiService, ExceptionService, FirebaseStateService, Status, UserService,
} from '@/core/services';

@Component({
  selector: 'ci-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.scss'],
})
export class LogComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  diff: Diff;
  repoId: string;
  log: string;
  status: Status;
  onloadSubscription = new Subscription();
  changesSubscription = new Subscription();

  constructor(
    private router: Router,
    private firebaseStateService: FirebaseStateService,
    private exceptionService: ExceptionService,
    private ciService: CiService,
    private userService: UserService,
  ) { }

  ngOnInit() {
    this.parseUrlParam();
  }

  // Gets parameters from url
  private parseUrlParam(): void {
    // '/log/33/my-project' -> [url, '33', 'my-project']
    const url: RegExpMatchArray = this.router.url.match(/\/log\/([\d]+)\/([\w\d\.\/-]+)?/);
    if (!url) {
      // User is trying to open '/log'
      this.router.navigate(['/']);
      return;
    }
    const diffId: string = url[1];
    this.repoId = url[2];
    this.loadDiff(diffId);
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
  setDiff(diff: Diff): void {
    if (diff === undefined) {
      this.exceptionService.diffNotFound();
      return;
    }
    this.diff = diff;
    this.getLog(this.diff, this.repoId);
  }

  getLog(diff: Diff, repoId: string): void {
    // If CI exists then load status from localserver
    if (this.diff.getCiResponseList()[0]) {
      this.ciService.loadCiLog(diff, repoId).subscribe(ciLog => {
        this.status = ciLog.status;
        this.log = ciLog.log;
        this.isLoading = false;
      });
    } else {
      // CI not found. Open diff
      this.router.navigate(['diff', this.diff.getId()]);
    }
  }

  getAuthor(): string {
    return this.userService.getUsername(this.diff.getAuthor().getEmail());
  }

  ngOnDestroy() {
    this.onloadSubscription.unsubscribe();
    this.changesSubscription.unsubscribe();
  }
}
