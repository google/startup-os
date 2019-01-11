import { Component, Input } from '@angular/core';

import { Diff } from '@/core/proto';
import { ThreadStateService } from './thread-state.service';

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

  constructor(private threadStateService: ThreadStateService) { }

  // Changes "Expand" button to "Collapse", when at least one comment is expanded
  commentExpanded(): void {
    this.isExpanded = true;
  }

  // Expands or collapses all threads
  toggleComments(): void {
    this.isExpanded = !this.isExpanded;
    this.threadStateService.toggle(this.isExpanded);
  }

  isThreadExist(): boolean {
    return this.diff.getDiffThreadList().length > 0 || this.diff.getCodeThreadList().length > 0;
  }
}
