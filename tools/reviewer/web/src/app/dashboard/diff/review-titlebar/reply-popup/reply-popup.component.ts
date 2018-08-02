import { Component, EventEmitter, Input, Output } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import {
  Author,
  Comment,
  Diff,
  Reviewer,
  Thread,
} from '@/shared/shell';
import { ReviewService } from '../../services';

@Component({
  selector: 'reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss'],
})
export class ReplyPopupComponent {
  message: string = '';
  approved: boolean = false;
  actionRequired: boolean = false;
  @Input() diff: Diff;
  @Output() submitted: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor(
    private authService: AuthService,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
    private reviewService: ReviewService,
  ) { }

  reply(): void {
    if (this.authService.userEmail === this.diff.getAuthor().getEmail()) {
      // Current user is the author
      const author: Author = this.diff.getAuthor();
      author.setNeedsAttention(false);

      // Set attention of all reviewers
      for (const reviewer of this.diff.getReviewerList()) {
        reviewer.setNeedsAttention(true);
      }
    } else {
      // Set attention of author
      const author: Author = this.diff.getAuthor();
      author.setNeedsAttention(true);

      // Get reviewer from userEmail
      const username: string = this.authService
        .getUsername(this.authService.userEmail);
      let reviewer: Reviewer = this.reviewService
        .getReviewerWithTheUsername(this.diff, username);

      if (!reviewer) {
        // Current user is not a reviewer; add the current user to reviewers
        reviewer = new Reviewer();
        reviewer.setEmail(username + '@gmail.com');

        // Default values
        reviewer.setApproved(false);

        // Add reviewer to Diff
        this.diff.addReviewer(reviewer);
      }
      // If Approved checkbox was checked
      if (this.approved) {
        reviewer.setApproved(true);
      }

      // Remove Attention of reviewer
      reviewer.setNeedsAttention(false);
    }

    // Add the message as a DiffThread
    this.message = this.message.trim();
    if (this.message) {
      const diffThread: Thread = new Thread();
      const comment: Comment = new Comment();
      comment.setContent(this.message);
      comment.setCreatedBy(this.authService.userEmail);
      comment.setTimestamp(Date.now());
      // Set isDone of Thread based on Action Required Checkbox
      diffThread.setIsDone(!this.actionRequired);
      diffThread.addComment(comment);
      this.diff.addDiffThread(diffThread);
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.submitted.emit();
      this.notificationService.success('Reply Submitted');
    }, () => {
      this.notificationService.error("Reply couldn't be submitted");
    });
  }
}
