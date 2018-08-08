import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';

// The component implements header of the diff
// How it looks: https://i.imgur.com/m6lZUjR.jpg
@Component({
  selector: 'diff-header',
  templateUrl: './diff-header.component.html',
})
export class DiffHeaderComponent {
  isReplyPopupDisplayed: boolean = false;
  @Input() diff: Diff;

  toggleReplyPopup(isReplyPopupDisplayed: boolean) {
    this.isReplyPopupDisplayed = isReplyPopupDisplayed;
  }
}
