import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Diff, File } from '@/core/proto';
import {
  DiffUpdateService,
  ExceptionService,
  FirebaseStateService,
  LocalserverService,
  NotificationService,
  UserService,
} from '@/core/services';
import { DeleteDiffDialogComponent, DeleteDiffReturn } from './delete-diff-dialog';

// The component implements diff page
// How it looks: https://i.imgur.com/nBGrGuc.jpg
@Component({
  selector: 'cr-diff',
  templateUrl: './diff.component.html',
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  diff: Diff;
  files: File[];
  onloadSubscription = new Subscription();
  changesSubscription = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private firebaseStateService: FirebaseStateService,
    private localserverService: LocalserverService,
    private exceptionService: ExceptionService,
    private notificationService: NotificationService,
    private userService: UserService,
    private diffUpdateService: DiffUpdateService,
  ) { }

  ngOnInit() {
    this.loadDiff(this.route.snapshot.params['id']);
  }

  // Loads diff from firebase
  loadDiff(id: string): void {
    this.onloadSubscription = this.firebaseStateService
      .getDiff(id)
      .subscribe(diff => {
        this.setDiff(diff);
        this.subscribeOnChanges();
      });
  }

  // Each time when diff is changed in firebase, we receive new diff here.
  subscribeOnChanges(): void {
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
    // Get files from localserver
    this.localserverService
      .getDiffFiles(this.diff.getId(), this.diff.getWorkspace())
      .subscribe(files => {
        this.files = files;
        this.isLoading = false;
      });
  }

  deleteDiff(): void {
    if (!this.isUserAuthorOfTheDiff()) {
      this.notificationService.error('Only author can delete a diff');
    } else if (this.isForbiddenStatus()) {
      this.notificationService.error('Diff with this status cannot be deleted');
    } else {
      // Check that user sure about it
      this.dialog.open(
        DeleteDiffDialogComponent,
        { data: this.diff },
      )
        .afterClosed()
        .subscribe((deleteDiffReturn: DeleteDiffReturn) => {
          // User answered
          if (deleteDiffReturn && deleteDiffReturn.isDeleteDiff) {
            // Delete the diff
            this.isLoading = true;
            this.ngOnDestroy();
            this.diffUpdateService.deleteDiff(this.diff);
            if (deleteDiffReturn.isDeleteWorkspace) {
              // TODO: delete workspace
            }
          }
        });
    }
  }

  isUserAuthorOfTheDiff(): boolean {
    return this.userService.email === this.diff.getAuthor().getEmail();
  }

  isForbiddenStatus(): boolean {
    return this.diff.getStatus() === Diff.Status.SUBMITTED ||
      this.diff.getStatus() === Diff.Status.SUBMITTING ||
      this.diff.getStatus() === Diff.Status.REVERTED ||
      this.diff.getStatus() === Diff.Status.REVERTING;
  }

  diffCanBeDeleted(): boolean {
    return this.isUserAuthorOfTheDiff() && !this.isForbiddenStatus();
  }

  ngOnDestroy() {
    this.onloadSubscription.unsubscribe();
    this.changesSubscription.unsubscribe();
  }
}
