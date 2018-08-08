import { Component, Input } from '@angular/core';

import { Comment, Thread } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { DiffService } from '../diff.service';

// The component implements UI of thread list of the diff
// How it looks: "/src/assets/design-blocks/diff-discussion.jpg"
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

  getUnresolvedThreads(): number {
    return this.threads.filter(thread => !thread.getIsDone()).length;
  }

  openFile(thread: Thread): void {
    this.diffService.openFile(thread.getFile(), this.diffId);
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }
}
