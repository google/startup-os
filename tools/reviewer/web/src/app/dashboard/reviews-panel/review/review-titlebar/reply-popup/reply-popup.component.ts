import { Component, Input, OnInit } from '@angular/core';

import { AuthService,
  FirebaseService,
  NotificationService,
  Reviewer,
} from '@/shared';
import { Diff } from '@/shared';

@Component({
  selector: 'cr-reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss'],
})
export class ReplyPopupComponent implements OnInit {
  @Input() diff: Diff;
  actionRequired = false;
  approved = false;

  constructor(
    private authService: AuthService,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  reply() {
    if (this.authService.userEmail === this.diff.getAuthor().getEmail()) {
      // current user is the author
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
      let reviewer = this.getReviewerWithTheUsername(username);

      if (!reviewer) {
        // Current user is not a reviewer; add the current user to reviewers
        // If reviewer not found, create new one.
        reviewer = new Reviewer();
        reviewer.setEmail(username + '@gmail.com');

        // Default values:
        reviewer.setApproved(false);
        reviewer.setNeedsAttention(false);

        const reviewers = this.diff.getReviewerList();
        reviewers.push(reviewer);
        // Is there a better way for just Adding a reviewer to Reviewer list?
        this.diff.setReviewerList(reviewers);
      }
      // If Approved checkbox was checked
      if (this.approved) {
        reviewer.setApproved(true);
      }
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Reply Submitted');
    }, () => {
      this.notificationService.error("Reply couldn't be submitted");
    });
  }

  // TODO ask Vadim about making a utility function for this
  getReviewerWithTheUsername(username: string): Reviewer {
    for (const reviewer of this.diff.getReviewerList()) {
      const reviewerUsername = this.authService
        .getUsername(reviewer.getEmail());

      if (reviewerUsername === username) {
        return reviewer;
      }
    }
  }
}
