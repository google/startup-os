import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';

// The component implements UI of discussions of a diff
// How it looks: https://i.imgur.com/p7YnDl8.jpg
@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
})
export class DiffDiscussionComponent {
  @Input() diff: Diff;
}
