import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { Diff, File } from '@/shared/proto';
import {
  AuthService,
  ExceptionService,
  FirebaseService,
  FirebaseStateService,
  LocalserverService,
  NotificationService,
} from '@/shared/services';
import { DiffService } from './diff.service';

// The component implements diff page
// How it looks: https://i.imgur.com/nBGrGuc.jpg
@Component({
  selector: 'cr-diff',
  templateUrl: './diff.component.html',
  providers: [DiffService],
  styleUrls: ['./diff.scss'],
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  diff: Diff;
  files: File[];
  onloadSubscription = new Subscription();
  changesSubscription = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private firebaseService: FirebaseService,
    private firebaseStateService: FirebaseStateService,
    private router: Router,
    private localserverService: LocalserverService,
    private exceptionService: ExceptionService,
    private notificationService: NotificationService,
    private authService: AuthService,
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
        // Hide deleted files
        this.files = files.filter(
          file => file.getAction() !== File.Action.DELETE,
        );
        this.isLoading = false;
      });
  }

  deleteDiff(): void {
    if (this.authService.userEmail !== this.diff.getAuthor().getEmail()) {
      this.notificationService.error('Only author can delete a diff');
    } else {
      if (confirm('Are you sure you want to delete this diff?')) {
        this.isLoading = true;
        this.ngOnDestroy();
        this.firebaseService.removeDiff(this.diff.getId().toString()).subscribe(() => {
          this.notificationService.success('Diff is deleted');
          this.router.navigate(['/diffs']);
        });
      }
    }
  }

  ngOnDestroy() {
    this.onloadSubscription.unsubscribe();
    this.changesSubscription.unsubscribe();
  }
}
