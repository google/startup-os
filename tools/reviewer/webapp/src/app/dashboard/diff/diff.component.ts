import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Diff, File } from '@/shared/proto';
import { FirebaseService, LocalserverService } from '@/shared/services';

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
  firebaseSubscription = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private firebaseService: FirebaseService,
    private localserverService: LocalserverService,
  ) { }

  ngOnInit() {
    const diffId: string = this.route.snapshot.params['id'];

    // Get diff from firebase
    this.firebaseSubscription = this.firebaseService
      .getDiff(diffId)
      .subscribe(diff => {
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
