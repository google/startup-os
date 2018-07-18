import { Component, Input, OnInit } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { Diff, Reviewer } from '@/shared/shell';
import { ReviewService } from '../services';

// The ReviewerListComponent is used to display reviewers
@Component({
  selector: 'reviewer-list',
  templateUrl: './reviewer-list.component.html',
  styleUrls: ['./reviewer-list.component.scss'],
})
export class ReviewerListComponent implements OnInit {
  // To show editable fields
  isEditing = false;
  usernames: string = '';

  @Input() diff: Diff;
  @Input() editable: boolean = true;

  constructor(
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private reviewService: ReviewService,
  ) { }

  ngOnInit() {
    this.getReviewerEmails();
  }

  getReviewerEmails(): void {
    this.usernames = this.diff.getReviewerList()
      .map(reviewer => this.authService.getUsername(reviewer.getEmail()))
      .join(', ');
  }

  // Save the new value of property and update Diff
  saveReviewers(): void {
    const reviewers: Reviewer[] = [];

    this.usernames
      .split(',')
      .map(reviewer => reviewer.trim())
      .filter(reviewer => reviewer.length)
      .forEach(username => {
        // TODO: Add username validation
        // e.g. no spaces and special chars etc
        // same in CC list
        let reviewer: Reviewer  = this.reviewService
          .getReviewerWithTheUsername(this.diff, username);
        if (!reviewer) {
          // If reviewer not found, create new one.
          reviewer = new Reviewer();
          // To support @google.com emails
          // we need to expand users functionality.
          // E.g. to create users collection in firestore.
          reviewer.setEmail(username + '@gmail.com');

          // Default values:
          reviewer.setApproved(false);
          reviewer.setNeedsAttention(true);
        }
        reviewers.push(reviewer);
      });

    this.diff.setReviewerList(reviewers);

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Reviewers saved');
    }, () => {
      this.notificationService.error("Reviewers can't be saved");
    });
  }

  // Request or cancel attention of the reviewer
  changeAttentionOfReviewer(reviewer: Reviewer): void {
    reviewer.setNeedsAttention(!reviewer.getNeedsAttention());

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const username = this.authService.getUsername(reviewer.getEmail());
      const message = reviewer.getNeedsAttention() ?
        `Attention of ${username} is requested` :
        `Attention of ${username} is canceled`;
      this.notificationService.success(message);
    }, () => {
      this.notificationService.error("Attention wasn't changed");
    });
  }
}
