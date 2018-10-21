import { Component, EventEmitter, Input, Output } from '@angular/core';
import { randstr64 } from 'rndmjs';

import {
  Comment,
  Diff,
  Reviewer,
  Thread,
} from '@/shared/proto';
import { AuthService, DiffUpdateService } from '@/shared/services';
import { DiffHeaderService } from '../diff-header.service';

// The popup appears when "Reply" button is pushed.
// How it looks: https://i.imgur.com/C8nXqyU.jpg
@Component({
  selector: 'reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss'],
  providers: [DiffHeaderService],
})
export class ReplyPopupComponent {
  isLoading: boolean = false;
  message: string = '';
  approved: boolean = true;
  actionRequired: boolean = false;
  @Input() diff: Diff;
  @Output() toggleReplyPopup = new EventEmitter<boolean>();

  constructor(
    private authService: AuthService,
    private diffUpdateService: DiffUpdateService,
    private diffHeaderService: DiffHeaderService,
  ) { }

  reply(): void {
    this.isLoading = true;
    this.diff.getAuthor().setNeedsAttention(true);

    if (this.authService.userEmail === this.diff.getAuthor().getEmail()) {
      // Current user is the author

      // Set attention of all reviewers
      for (const reviewer of this.diff.getReviewerList()) {
        reviewer.setNeedsAttention(true);
      }
    } else {
      // Current user isn't the author

      // Get reviewer from user email
      let reviewer: Reviewer = this.diffHeaderService.getReviewer(
        this.diff,
        this.authService.userEmail,
      );

      if (!reviewer) {
        // If current user isn't present in reviewer list,
        // then create new reviewer
        reviewer = new Reviewer();
        reviewer.setEmail(this.authService.userEmail);
        this.diff.addReviewer(reviewer);
      }

      reviewer.setApproved(this.approved);
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
      diffThread.setType(Thread.Type.DIFF);
      diffThread.setId(randstr64(6));
      this.diff.addDiffThread(diffThread);
    }

    // Send the update to firebase
    this.diffUpdateService.submitReply(this.diff).subscribe(() => this.closeReplyPopup());
  }

  closeReplyPopup(): void {
    this.toggleReplyPopup.emit(false);
  }
}
