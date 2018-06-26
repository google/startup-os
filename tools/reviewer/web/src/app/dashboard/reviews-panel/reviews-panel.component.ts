import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService, FirebaseService, Lists } from '@/shared';
import { Diff, Reviewer } from '@/shared/shell/proto/code-review_pb';

@Component({
  selector: 'app-reviews-panel',
  templateUrl: './reviews-panel.component.html',
  styleUrls: ['./reviews-panel.component.scss']
})
export class ReviewsPanelComponent implements OnInit {
  isLoading: boolean = true;
  diffGroups: Array<Array<Diff.AsObject>> = [[], [], [], []];

  constructor(
    private firebaseService: FirebaseService,
    private authService: AuthService,
    private router: Router,
  ) { }

  ngOnInit() {
    // Get username from url if there is no user.
    // Get the loggedIn username from its email.
    this.getReviews();
  }

  // Get diff/reviews from Database
  getReviews() {
    this.firebaseService.getDiffs().subscribe(
      diffs => {
        // Diffs are categorized in 4 different lists
        this.diffGroups[Lists.NeedAttention] = [];
        this.diffGroups[Lists.CcedOn] = [];
        this.diffGroups[Lists.DraftReviews] = [];
        this.diffGroups[Lists.SubmittedReviews] = [];

        // Iterate over each object in res
        // and create Diff from proto and categorize
        // into a specific list.
        for (const diff of diffs) {
          for (const reviewer of diff.getReviewerList()) {
            if (reviewer.getEmail() === this.authService.userEmail) {
              if (reviewer.getNeedsattention()) {
                // Need attention of user
                this.diffGroups[Lists.NeedAttention].push(diff.toObject());
              } else {
                // User is cc'ed on this
                this.diffGroups[Lists.CcedOn].push(diff.toObject());
              }
            } else if (reviewer.getEmail() === this.authService.userEmail) {
              if (diff.getStatus() === Diff.Status.REVIEW_NOT_STARTED) {
                // Draft Review
                this.diffGroups[Lists.NeedAttention].push(diff.toObject());
              } else if (diff.getStatus() === Diff.Status.SUBMITTED) {
                // Submitted Review
                this.diffGroups[Lists.CcedOn].push(diff.toObject());
              }
            }
          }
        }

        this.sortDiffs();
        this.isLoading = false;
      },
      () => {
        // Permission Denied
      }
    );
  }

  // Sort the diffs based on their date modified
  sortDiffs(): void {
    for (const diffList of this.diffGroups) {
      diffList.sort((a, b) => {
        return Math.sign(b.modifiedTimestamp - a.modifiedTimestamp);
      });
    }
  }

  // Navigate to a Diff
  diffClicked(diffId: number): void {
    this.router.navigate(['diff/', diffId]);
  }

  getUsernames(reviewerList: Reviewer.AsObject[]): string {
    return reviewerList
      .map(reviewer => this.authService.getUsername(reviewer.email))
      .join(', ');
  }
}
