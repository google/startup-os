import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';
import { ThreadState, ThreadStateService } from './thread';

// The component implements UI of discussions of a diff
// How it looks: https://i.imgur.com/p7YnDl8.jpg
@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
  styleUrls: ['./diff-discussion.component.scss'],
})
export class DiffDiscussionComponent {
  isExpanded: boolean = false;

  @Input() diff: Diff;

  constructor(private threadStateService: ThreadStateService) {
    this.threadStateService.reset();

    this.threadStateService.openCommentChanges.subscribe(() => {
      this.isExpanded = true;
    });
  }

  // Expand or collapse threads
  toggleComments(): void {
    this.isExpanded = !this.isExpanded;
    for (const id in this.threadStateService.threadStateMap) {
      const threadState: ThreadState = this.threadStateService.threadStateMap[id];
      threadState.isCommentOpenMap = threadState.isCommentOpenMap.map(() => this.isExpanded);
    }
    this.threadStateService.updateState();
  }

  isThreadExist(): boolean {
    return this.diff.getDiffThreadList().length > 0 || this.diff.getCodeThreadList().length > 0;
  }
}
