import { Component, Input, OnInit } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService
} from '@/shared/services';
import { Diff } from '@/shared/shell';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss']
})
export class ReviewTitlebarComponent implements OnInit {
  statusList = [
    'Review Not Started',
    'Needs More Work',
    'Under Review',
    'Accepted',
    'Submitting',
    'Submitted',
    'Reverting',
    'Reverted',
  ];
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

  updateAuthor(): void {
    // Update need attention of the author in the diff
    const author = this.diff.getAuthor();
    author.setNeedsattention(!author.getNeedsattention());
    this.diff.setAuthor(author);

    // Send updated diff to firebase
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Attention is updated');
    }, () => {
      this.notificationService.error('Error');
    });
  }
}
