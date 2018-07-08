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

  ngOnInit() {
    console.log(this.diff);
    console.log(this.authService.userEmail);
    console.log(this.diff.getAuthor().getEmail());
  }

  reply() {
    console.log(this.diff);
    if (this.authService.userEmail === this.diff.getAuthor().getEmail()) {
      // current user is the author
      const author = this.diff.getAuthor();
      author.setNeedsattention(false);

      // Set attention of all reviewers
      for (const reviewer of this.diff.getReviewerList()) {
        reviewer.setNeedsattention(true);
      }
    } else {
      // Current user is a reviewer
      // Set attention of author
      const author = this.diff.getAuthor();
      author.setNeedsattention(true);

      // Get reviewer from userEmail
      const username = this.authService
        .getUsername(this.authService.userEmail);
      const reviewer = this.getReviewerWithTheUsername(username);
      if (reviewer) {
        reviewer.setNeedsattention(false);
      }
      if (this.approved) {
        reviewer.setApproved(true);
      }
    }

    console.log(this.diff);

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Reviewers saved');
    }, () => {
      this.notificationService.error("Reviewers can't be saved");
    });
  }

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
