import { Component, Input } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService,
  Reviewer,
  ReviewService,
} from '@/shared';
import { Diff } from '@/shared';

@Component({
  selector: 'cr-reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss'],
})
export class ReplyPopupComponent {
  @Input() diff: Diff;
  approved = false;

  constructor(
    private authService: AuthService,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
    private reviewService: ReviewService,
  ) { }

  reply(): void {
    if (this.authService.userEmail === this.diff.getAuthor().getEmail()) {
      // Current user is the author
      const author = this.diff.getAuthor();
      author.setNeedsAttention(false);

      // Set attention of all reviewers
      for (const reviewer of this.diff.getReviewerList()) {
        reviewer.setNeedsAttention(true);
      }
    } else {
      // Set attention of author
      const author = this.diff.getAuthor();
      author.setNeedsAttention(true);

      // Get reviewer from userEmail
      const username = this.authService
        .getUsername(this.authService.userEmail);
      let reviewer = this.reviewService
        .getReviewerWithTheUsername(this.diff, username);

      if (!reviewer) {
        // Current user is not a reviewer; add the current user to reviewers
        reviewer = new Reviewer();
        reviewer.setEmail(username + '@gmail.com');

        // Default values:
        reviewer.setApproved(false);

        const reviewers = this.diff.getReviewerList();
        reviewers.push(reviewer);

        // TODO
        // Is there a better way for just Adding a reviewer to Reviewer list?
        this.diff.setReviewerList(reviewers);
      }
      // If Approved checkbox was checked
      if (this.approved) {
        reviewer.setApproved(true);
      }

      // Remove Attention of reviewer
      reviewer.setNeedsAttention(false);
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Reply Submitted');
    }, () => {
      this.notificationService.error("Reply couldn't be submitted");
    });
  }
}
