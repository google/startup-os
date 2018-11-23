import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { randstr64 } from 'rndmjs';

import {
  Comment,
  Diff,
  Reviewer,
  Thread,
} from '@/core/proto';
import { DiffUpdateService, UserService } from '@/core/services';
import { DiffHeaderService } from '../diff-header.service';

// The popup appears when "Reply" button is pushed.
// How it looks: https://i.imgur.com/C8nXqyU.jpg
@Component({
  selector: 'reply-popup',
  templateUrl: './reply-popup.component.html',
  styleUrls: ['./reply-popup.component.scss'],
  providers: [DiffHeaderService],
})
export class ReplyPopupComponent implements OnInit {
  isLoading: boolean = false;
  message: string = '';
  approved: boolean = false;
  actionRequired: boolean = false;
  reviewer: Reviewer;
  @Input() diff: Diff;
  @Output() toggleReplyPopup = new EventEmitter<boolean>();

  constructor(
    private userService: UserService,
    private diffUpdateService: DiffUpdateService,
    private diffHeaderService: DiffHeaderService,
  ) { }

  ngOnInit() {
    // Set "Approved" checkbox to current approve value
    this.reviewer = this.diffHeaderService.getReviewer(
      this.diff,
      this.userService.email,
    );
    if (this.reviewer) {
      // Only if current user is present in reviewer list
      this.approved = this.reviewer.getApproved();
    }
  }

  reply(): void {
    this.isLoading = true;
    this.diff.getAuthor().setNeedsAttention(true);

    if (this.userService.email === this.diff.getAuthor().getEmail()) {
      // Current user is an author

      // Set attention of all reviewers
      for (const reviewer of this.diff.getReviewerList()) {
        reviewer.setNeedsAttention(true);
      }
    } else {
      // Current user isn't an author

      if (!this.reviewer) {
        // If current user isn't present in reviewer list,
        // then create new reviewer
        this.reviewer = new Reviewer();
        this.reviewer.setEmail(this.userService.email);
        this.diff.addReviewer(this.reviewer);
      }

      this.reviewer.setApproved(this.approved);
      this.reviewer.setNeedsAttention(false);
    }

    // Add the message as a DiffThread
    this.message = this.message.trim();
    if (this.message) {
      const diffThread: Thread = new Thread();
      const comment: Comment = new Comment();
      comment.setContent(this.message);
      comment.setCreatedBy(this.userService.email);
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
