import { Component, Input } from '@angular/core';

import { Comment, Thread } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { DiffService } from '../diff.service';

@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
  styleUrls: ['./diff-discussion.component.scss'],
  providers: [DiffService],
})
export class DiffDiscussionComponent {
  displayedColumns = ['discussions'];
  @Input() threads: Thread[];
  @Input() diffId: number;

  constructor(
    public diffService: DiffService,
    public authService: AuthService,
  ) { }

  getAmountOfComments(): number {
    let amount: number = 0;
    for (const thread of this.threads) {
      amount += thread.getCommentList().length;
    }
    return amount;
  }

  openFile(thread: Thread): void {
    this.diffService.openFile(thread.getFile(), this.diffId);
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }
}
