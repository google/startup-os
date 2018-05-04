import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService
} from '@/shared';
import { Component, NgZone, OnInit, Renderer2 } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit {
  diffId: string;
  diff: Diff;
  reviewers: string = '';
  ccs: string = '';
  bug: string = '';
  notEditable: boolean = false;
  notEditableStatus: Array<number> = [4, 5];
  showEditableReviewers = false;
  showEditableCCs = false;
  showEditableBug = false;

  constructor(
    private route: ActivatedRoute,
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private zone: NgZone,
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
        this.getReviewers();
        this.getCCs();
        this.getBug();
        this.notEditable = !this.notEditableStatus.includes(this.diff.status);
      });
    });
  }

  openFile(filePosition): void {
    this.zone.run(() => {
      // Build a route path on the following format
      // /diff/<diff number>/<path>?
      // ls=<left snapshot number>&rs=<right snapshot number>
      this.router.navigate(['diff/' + this.diffId + '/' + filePosition], {
        queryParams: { ls: '1', rs: '3' }
      });
    });
  }

  getReviewers(): void {
    const diffReviewers = this.diff.reviewers;
    this.reviewers = '';
    for (let i = 0; i < diffReviewers.length; i++) {
      this.reviewers =
        this.reviewers +
        (i === diffReviewers.length - 1
          ? diffReviewers[i]
          : diffReviewers[i] + ', ');
    }
  }

  getCCs(): void {
    const diffCCs = this.diff.cc;
    this.ccs = '';
    for (let i = 0; i < diffCCs.length; i++) {
      this.ccs =
        this.ccs + (i === diffCCs.length - 1 ? diffCCs[i] : diffCCs[i] + ', ');
    }
  }

  saveReviewers(): void {
    let reviewers = this.reviewers.split(',');
    reviewers = reviewers.map(v => v.trim());
    this.diff.reviewers = reviewers;
    this.updateDiff(this.diff, 'Reviewers Saved');
  }

  saveCCs(): void {
    let ccs = this.ccs.split(',');
    ccs = ccs.map(v => v.trim());
    this.diff.cc = ccs;
    this.updateDiff(this.diff, 'CCs Saved');
  }

  getBug(): void {
    this.bug = this.diff.bug;
  }

  saveBug(): void {
    this.diff.bug = this.bug;
    this.updateDiff(this.diff, 'Bug Updated');
  }

  updateAttentionList(name: string): void {
    if (this.diff.needAttentionOf.includes(name)) {
      this.diff.needAttentionOf = this.diff.needAttentionOf.filter(
        e => e !== name
      );
    } else {
      this.diff.needAttentionOf.push(name);
    }
    this.updateDiff(this.diff, 'Need Attention List Updated');
  }

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

  getButtonText(reviewer: string): string {
    return this.diff.needAttentionOf.indexOf(reviewer) > -1
      ? 'Remove From Needs Attention'
      : 'Add to Needs Attention';
  }
}
