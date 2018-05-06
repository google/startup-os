import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService
} from '@/shared';
import { Component, OnInit, Renderer2 } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit {
  diffId: string;
  diff: Diff;

  constructor(
    private route: ActivatedRoute,
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit() {
    this.diffId = this.route.snapshot.params['id'];

    // Get a single review
    this.firebaseService.getDiff(this.diffId).subscribe(res => {
      // Create Diff from proto
      this.protoService.open.subscribe(error => {
        if (error) {
          throw error;
        }
        const review = res;
        this.diff = this.protoService.createDiff(review);
        this.diff.number = parseInt(this.diffId, 10);
      });
    });
  }

  // Upon click on a file open a
  // single file reivew page showing
  // code difference and comments
  openFile(filePosition): void {
    // Build a route path on the following format
    // /diff/<diff number>/<path>?
    // ls=<left snapshot number>&rs=<right snapshot number>
    this.router.navigate(['diff/' + this.diffId + '/' + filePosition], {
      queryParams: { ls: '1', rs: '3' }
    });
  }

  // Update the Diff in the DB
  updateDiff(diff: Diff, message: string): void {
    this.firebaseService
      .updateDiff(diff)
      .then(res => {
        // TODO make separate service for notifications
        this.notify.success(message);
      })
      .catch(err => {
        this.notify.error('Some Error Occured');
      });
  }

  // Get text for 'Add to Attention List'
  // and 'Remove from Attention List'
  getButtonText(reviewer: string): string {
    return this.diff.needAttentionOf.indexOf(reviewer) > -1
      ? 'Remove From Needs Attention'
      : 'Add to Needs Attention';
  }
}
