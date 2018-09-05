import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService, FirebaseService } from '@/shared';
import { Diff, Reviewer } from '@/shared/proto';

export enum DiffGroups {
  NeedAttention,
  Incoming,
  Outgoing,
  CC,
  Draft,
  Pending,
  Submitted,
}

@Component({
  selector: 'cr-diffs',
  templateUrl: './diffs.component.html',
  styleUrls: ['./diffs.component.scss'],
})
export class DiffsComponent implements OnInit {
  isLoading: boolean = true;
  diffGroups: Diff[][] = [];
  displayedColumns: string[] = [
    'id',
    'author',
    'status',
    'action',
    'workspace',
    'reviewers',
    'description',
  ];
  diffGroupNameList: string[] = [];

  constructor(
    private firebaseService: FirebaseService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.diffGroupNameList[DiffGroups.NeedAttention] = 'Need Attention';
    this.diffGroupNameList[DiffGroups.Incoming] = 'Incoming Diffs';
    this.diffGroupNameList[DiffGroups.Outgoing] = 'Outgoing Diffs';
    this.diffGroupNameList[DiffGroups.CC] = "CC'ed Diffs";
    this.diffGroupNameList[DiffGroups.Draft] = 'Draft Diffs';
    this.diffGroupNameList[DiffGroups.Pending] = 'Pending Diffs';
    this.diffGroupNameList[DiffGroups.Submitted] = 'Submitted Diffs';
  }

  ngOnInit() {
    const urlEmail: string = this.router
      .parseUrl(this.router.url)
      .queryParams['email'];

    if (urlEmail) {
      // Show the page from a view of the user from url.
      this.getReviews(urlEmail);
    } else {
      // Show the page from current login view.
      this.getReviews(this.authService.userEmail);
    }
  }

  // Get diff/reviews from Database
  getReviews(userEmail: string) {
    this.firebaseService.getDiffs().subscribe(
      diffs => {
        // Diffs are categorized in 4 different lists
        this.diffGroups[DiffGroups.NeedAttention] = [];
        this.diffGroups[DiffGroups.Incoming] = [];
        this.diffGroups[DiffGroups.Outgoing] = [];
        this.diffGroups[DiffGroups.CC] = [];
        this.diffGroups[DiffGroups.Draft] = [];
        this.diffGroups[DiffGroups.Pending] = [];
        this.diffGroups[DiffGroups.Submitted] = [];

        // Iterate over each object in res
        // and create Diff from proto and categorize
        // into a specific list.
        for (const diff of diffs) {
          // needAttentionOfList is a list of all reviewers,
          // where attention is requested
          const needAttentionOfList: string[] = diff.getReviewerList()
            .filter(reviewer => reviewer.getNeedsAttention())
            .map(reviewer => reviewer.getEmail());

          if (diff.getAuthor().getEmail() === userEmail) {
            // Current user is an author of the diff
            this.diffGroups[DiffGroups.Outgoing].push(diff);

            switch (diff.getStatus()) {
              case Diff.Status.SUBMITTED:
                // Submitted Review
                this.diffGroups[DiffGroups.Submitted].push(diff);
                break;
              case Diff.Status.REVIEW_NOT_STARTED:
                // Draft Review
                this.diffGroups[DiffGroups.Draft].push(diff);
                break;
            }
            // Attention of current user as an author is requested
            if (diff.getAuthor().getNeedsAttention()) {
              this.diffGroups[DiffGroups.NeedAttention].push(diff);
            }
          } else if (needAttentionOfList.includes(userEmail)) {
            // Need attention of user
            this.diffGroups[DiffGroups.NeedAttention].push(diff);
            this.diffGroups[DiffGroups.Incoming].push(diff);
          } else if (diff.getCcList().includes(userEmail)) {
            // User is cc'ed on this
            this.diffGroups[DiffGroups.CC].push(diff);
          }
        }

        this.sortDiffs();
        this.isLoading = false;
      },
    );
  }

  // Sort the diffs based on their date modified
  sortDiffs(): void {
    for (const diffList of this.diffGroups) {
      diffList.sort((a, b) => {
        // Newest first
        return Math.sign(b.getModifiedTimestamp() - a.getModifiedTimestamp());
      });
    }
  }

  // Navigate to a Diff
  openDiff(diffId: number): void {
    this.router.navigate(['diff/', diffId]);
  }

  getUsernames(reviewerList: Reviewer[]): string {
    return reviewerList
      .map(reviewer => this.authService.getUsername(reviewer.getEmail()))
      .join(', ');
  }
}
