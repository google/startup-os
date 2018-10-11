import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';
import { ThreadStateService } from './thread';

// The component implements UI of discussions of a diff
// How it looks: https://i.imgur.com/p7YnDl8.jpg
@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
  styleUrls: ['./diff-discussion.component.scss'],
})
export class DiffDiscussionComponent {
  @Input() diff: Diff;

  constructor(private threadStateService: ThreadStateService) {
    this.threadStateService.reset();
  }
}
