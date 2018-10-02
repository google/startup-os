import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Diff, File } from '@/shared/proto';
import {
  ExceptionService,
  FirebaseService,
  LocalserverService,
} from '@/shared/services';
import { DiffService } from './diff.service';

// The component implements diff page
// How it looks: https://i.imgur.com/nBGrGuc.jpg
@Component({
  selector: 'cr-diff',
  templateUrl: './diff.component.html',
  providers: [DiffService],
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  diff: Diff;
  files: File[];
  firebaseSubscription = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private firebaseService: FirebaseService,
    private localserverService: LocalserverService,
    private exceptionService: ExceptionService,
  ) { }

  ngOnInit() {
    const diffId: string = this.route.snapshot.params['id'];

    // Get diff from firebase
    this.firebaseSubscription = this.firebaseService
      .getDiff(diffId)
      .subscribe(diff => {
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
      });
  }

  ngOnDestroy() {
    this.firebaseSubscription.unsubscribe();
  }
}
