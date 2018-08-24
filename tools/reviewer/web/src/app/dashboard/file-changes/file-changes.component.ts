import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  BranchInfo,
  Comment,
  Diff,
  File,
  TextDiff,
  Thread,
} from '@/shared/proto';
import {
  DifferenceService,
  FirebaseService,
  LocalserverService,
  NotificationService,
} from '@/shared/services';
import { FileChangesService } from './file-changes.service';

// The component implements a diff
@Component({
  selector: 'file-changes',
  templateUrl: './file-changes.component.html',
  styleUrls: ['./file-changes.component.scss'],
})
export class FileChangesComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  textDiff: TextDiff;
  changes: number[];
  localThreads: Thread[];
  diff: Diff;
  addCommentSubscription: Subscription;
  deleteCommentSubscription: Subscription;
  file: File = new File();
  branchInfo: BranchInfo;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private fileChangesService: FileChangesService,
    private localserverService: LocalserverService,
    private notificationService: NotificationService,
  ) {
    this.addCommentSubscription = this.fileChangesService
      .addCommentChanges.subscribe(param => {
        this.addComment(param.lineNumber, param.comments);
      });

    this.deleteCommentSubscription = this.fileChangesService.
      deleteCommentChanges.subscribe(isDeleteThread => {
        this.deleteComment(isDeleteThread);
      });
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot: ActivatedRouteSnapshot = this.activatedRoute.snapshot;
    const filename: string = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');
    this.file.setFilename(filename);

    this.getDiff(urlSnapshot.url[0].path);
  }

  ngOnDestroy() {
    this.addCommentSubscription.unsubscribe();
  }
}
