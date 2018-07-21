import { Component, Input, OnInit } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService } from '@/shared/services';
import { Author, Diff } from '@/shared/shell';
import { statusList } from './status-ui';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss'],
})
export class ReviewTitlebarComponent implements OnInit {
  statusList = statusList;
  isLoading: boolean = true;
  isReplyDialogShown: boolean = false;
  @Input() diff: Diff;
  @Input() editable;

  constructor(
    public authService: AuthService,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  ngOnInit() {
    this.isLoading = false;
  }

  // Request or cancel attention of the author
  changeAttentionOfAuthor(): void {
    const author: Author = this.diff.getAuthor();
    author.setNeedsAttention(!author.getNeedsAttention());

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const message: string = this.diff.getAuthor().getNeedsAttention() ?
        'Attention of author is requested' :
        'Attention of author is canceled';
      this.notificationService.success(message);
    }, () => {
      this.notificationService.error('Error');
    });
  }

  // Hide the Reply form when it's submitted successfully
  replySubmitted(): void {
    this.isReplyDialogShown = false;
  }
}
