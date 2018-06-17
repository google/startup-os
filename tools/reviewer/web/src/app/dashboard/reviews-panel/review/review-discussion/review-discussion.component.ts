import { Diff } from '@/shared';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'review-discussion',
  templateUrl: './review-discussion.component.html',
  styleUrls: ['./review-discussion.component.scss']
})
export class ReviewDiscussionComponent {
  @Input() diff: Diff.AsObject;

  getTotalComments(): number {
    return this.diff.threadsList
      .map(v => v.commentsList.length)
      .reduce((a, b) => a + b, 0);
  }

  getUnresolvedComments(): number {
    return this.diff.threadsList.filter(v => !v.isDone).length;
  }
}
