import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService, FirebaseService, Lists } from '@/shared';
import { Diff, Reviewer } from '@/shared/shell/proto/code-review_pb';

@Component({
  selector: 'app-reviews-panel',
  templateUrl: './reviews-panel.component.html',
  styleUrls: ['./reviews-panel.component.scss'],
})
export class ReviewsPanelComponent {
  isLoading: boolean = true;
  diffGroups: Diff[][] = [];

  constructor(
    private firebaseService: FirebaseService,
    private authService: AuthService,
    private router: Router,
  ) {
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
          const you: string = this.authService.userEmail;
          // needAttentionOfList is list of author and all reviewers,
          // where attention is requested
          const needAttentionOfList: string[] = diff.getReviewerList()
            .filter(reviewer => reviewer.getNeedsattention())
            .map(reviewer => reviewer.getEmail());
          if (diff.getAuthor().getNeedsattention()) {
            needAttentionOfList.concat(diff.getAuthor().getEmail());
          }

          if (needAttentionOfList.includes(you)) {
            // Need attention of user
            this.diffGroups[Lists.NeedAttention].push(diff);
          } else if (diff.getCcList().includes(you)) {
            // User is cc'ed on this
            this.diffGroups[Lists.CcedOn].push(diff);
          } else if (diff.getAuthor().getEmail() === you) {
            switch (diff.getStatus()) {
              case Diff.Status.REVIEW_NOT_STARTED:
                // Draft Review
                this.diffGroups[Lists.NeedAttention].push(diff);
                break;
              case Diff.Status.SUBMITTED:
                // Submitted Review
                this.diffGroups[Lists.CcedOn].push(diff);
                break;
            }
          }
        }

        this.sortDiffs();
        this.isLoading = false;
      },
      () => {
        // Permission Denied
      },
    );
  }

  // Sort the diffs based on their date modified
  sortDiffs(): void {
    for (const diffList of this.diffGroups) {
      diffList.sort((a, b) => {
        return Math.sign(b.getModifiedTimestamp() - a.getModifiedTimestamp());
      });
    }
  }

  // Navigate to a Diff
  diffClicked(diffId: number): void {
    this.router.navigate(['diff/', diffId]);
  }

  getUsernames(reviewerList: Reviewer[]): string {
    return reviewerList
      .map(reviewer => this.authService.getUsername(reviewer.getEmail()))
      .join(', ');
  }
}
