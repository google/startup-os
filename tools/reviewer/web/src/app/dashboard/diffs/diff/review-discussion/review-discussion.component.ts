import { Component, Input } from '@angular/core';

import { AuthService } from '@/shared/services';
import { Diff } from '@/shared/shell';

@Component({
  selector: 'review-discussion',
  templateUrl: './review-discussion.component.html',
  styleUrls: ['./review-discussion.component.scss'],
})
export class ReviewDiscussionComponent {
  @Input() diff: Diff;

  constructor(
    public authService: AuthService,
  ) { }

  getTotalComments(): number {
    return this.diff.getThreadList()
      .map(thread => thread.getCommentList().length)
      .reduce((a, b) => a + b, 0);
  }

  getUnresolvedComments(): number {
    return this.diff.getThreadList()
      .filter(thread => !thread.getIsDone()).length;
  }
}
