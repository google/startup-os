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
      this.categorizeDiffs(urlEmail);
    } else {
      // Show the page from current login view.
      this.categorizeDiffs(this.authService.userEmail);
    }
  }

  // Categorize diffs in specific groups
  categorizeDiffs(userEmail: string) {
    this.firebaseService.getDiffs().subscribe(diffs => {
      this.diffGroups[DiffGroups.NeedAttention] = [];
      this.diffGroups[DiffGroups.Incoming] = [];
      this.diffGroups[DiffGroups.Outgoing] = [];
      this.diffGroups[DiffGroups.CC] = [];
      this.diffGroups[DiffGroups.Draft] = [];
      this.diffGroups[DiffGroups.Pending] = [];
      this.diffGroups[DiffGroups.Submitted] = [];

      for (const diff of diffs) {
        if (diff.getAuthor().getEmail() === userEmail) {
          // Current user is an author of the diff
          switch (diff.getStatus()) {
            case Diff.Status.UNDER_REVIEW:
            case Diff.Status.NEEDS_MORE_WORK:
            case Diff.Status.ACCEPTED:
            case Diff.Status.SUBMITTING:
            case Diff.Status.REVERTING:
              // Outgoing Review
              this.diffGroups[DiffGroups.Outgoing].push(diff);
              break;
            case Diff.Status.SUBMITTED:
            case Diff.Status.REVERTED:
              // Submitted Review
              this.diffGroups[DiffGroups.Submitted].push(diff);
              break;
            case Diff.Status.REVIEW_NOT_STARTED:
              // Draft Review
              this.diffGroups[DiffGroups.Draft].push(diff);
              break;
          }

          if (diff.getAuthor().getNeedsAttention()) {
            // Attention of the author is requested
            this.diffGroups[DiffGroups.NeedAttention].push(diff);
          }
        } else if (diff.getCcList().includes(userEmail)) {
          // User is cc'ed on this
          this.diffGroups[DiffGroups.CC].push(diff);
        } else {
          // Current user is neither an author nor CC
          // Maybe reviewer?
          for (const reviewer of diff.getReviewerList()) {
            if (reviewer.getEmail() === userEmail) {
              // Current user is a reviewer
              this.diffGroups[DiffGroups.Incoming].push(diff);
              if (reviewer.getNeedsAttention()) {
                // Attention of the reviewer is requested
                this.diffGroups[DiffGroups.NeedAttention].push(diff);
              }
              break;
            }
          }
        }
      }

      this.sortDiffs();
      this.isLoading = false;
    });
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
