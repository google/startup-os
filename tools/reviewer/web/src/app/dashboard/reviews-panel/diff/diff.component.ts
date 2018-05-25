import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { Comment, Diff, Thread } from '@/shared';
import {
  DifferenceService,
  FirebaseService,
  ProtoService
} from '@/shared/services';
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
  filePath: string;
  files: string[] = files;
  changes: number[];
  threads: Thread[];
  snapshot: number;
  diff: Diff;
  newCommentSubscription: Subscription;
  diffId: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private differenceService: DifferenceService,
    private firebaseService: FirebaseService,
    private diffService: DiffService,
    private protoService: ProtoService
  ) {
    this.changes = this.differenceService.compare(files[0], files[1]);

    this.newCommentSubscription = this.diffService.newComment.subscribe(
      param => {
        // New comment is added
        this.addComment(param.lineNumber, param.comments);
      }
    );
  }

  addComment(lineNumber: number, comments: Comment[]): void {
    if (comments.length === 1) {
      this.diff.threads.push({
        snapshot: this.snapshot,
        lineNumber: lineNumber,
        filename: this.filePath,
        comments: comments,
        isDone: false
      });
      this.filtherThreads(this.diff.threads);
    }

    this.protoService.open.subscribe(() => {
      const protoDiff = this.protoService.createDiff(this.diff);
      this.firebaseService.updateDiff(protoDiff).subscribe(
        () => {
          // Success
        },
        () => {
          // Access denied
        }
      );
    });
  }

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    this.filePath = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');

    this.snapshot = parseInt(urlSnapshot.queryParams['rs'], 10) || null;

    this.diffId = urlSnapshot.url[0].path;
    this.firebaseService.getDiff(this.diffId).subscribe(
      diff => {
        this.diff = diff;
        this.filtherThreads(diff.threads);
        this.isLoading = false;
      },
      () => {
        // Access denied
      }
    );
  }

  filtherThreads(treads: Thread[]): void {
    this.threads = treads.filter(v => v.filename === this.filePath);
  }

  ngOnDestroy() {
    this.newCommentSubscription.unsubscribe();
  }
}
