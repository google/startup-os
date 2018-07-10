import { Component, Input, OnInit } from '@angular/core';

import { FirebaseService, NotificationService } from '@/shared/services';

import {
  AuthService,
} from '@/shared/services';
import { Diff } from '@/shared/shell';
import { statusList } from './status-ui';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss'],
})
export class ReviewTitlebarComponent implements OnInit {
  statusList = statusList;
  isLoading: boolean = true;
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
    const author = this.diff.getAuthor();
    author.setNeedsAttention(!author.getNeedsAttention());

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const message = this.diff.getAuthor().getNeedsAttention() ?
        'Attention of author is requested' :
        'Attention of author is canceled';
      this.notificationService.success(message);
    }, () => {
      this.notificationService.error('Error');
    });
  }
}
