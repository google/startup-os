import { Diff } from '@/shared/services';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'review-discussion',
  templateUrl: './review-discussion.component.html',
  styleUrls: ['./review-discussion.component.scss']
})
export class ReviewDiscussionComponent {
  @Input() diff: Diff;

  getTotalComments(): number {
    return this.diff.threads
      .map(v => v.comments.length)
      .reduce((a, b) => a + b, 0);
  }

  getUnresolvedComments(): number {
    return this.diff.threads.filter(v => !v.isDone).length;
  }
}
