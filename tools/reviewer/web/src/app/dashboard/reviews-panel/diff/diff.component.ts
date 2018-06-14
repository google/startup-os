import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { Comment, Diff, Thread } from '@/shared';
import { DifferenceService, FirebaseService } from '@/shared/services';
import { DiffService } from './diff.service';

// TODO: load files from server
import { files } from './mock-files';

// The component implements a diff
@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss']
})
export class DiffComponent implements OnInit, OnDestroy {
  isLoading: boolean = true;
  filename: string;
  files: string[] = files;
  changes: number[];
  snapshot: number;
  localThreads: Thread[];
  diff: Diff;
  newCommentSubscription: Subscription;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService
  ) {
    this.changes = this.differenceService.compare(files[0], files[1]);

    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        this.addComment(param.lineNumber, param.comments);
      }
    );
  }

  addComment(lineNumber: number, comments: Comment[]): void {
    if (comments.length === 1) {
      // Create a thread, if it's first comment
      const newThread = new Thread();
      newThread.setSnapshot(this.snapshot);
      newThread.setLineNumber(lineNumber);
      newThread.setFilename(this.filename);
      newThread.setIsDone(false);
      newThread.setCommentsList(comments);
      this.diff.addThreads(newThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe();
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    this.filename = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');

    this.snapshot = parseInt(urlSnapshot.queryParams['rs'], 10) || null;

    const diffId = urlSnapshot.url[0].path;
    this.firebaseService.getDiff(diffId).subscribe(
      diff => {
        this.diff = diff;
        this.localThreads = this.diff
          .getThreadsList()
          .filter(v => v.getFilename() === this.filename);

        this.isLoading = false;
      },
      () => {
        // Access denied
      }
    );
  }

  ngOnDestroy() {
    this.newCommentSubscription.unsubscribe();
  }
}
