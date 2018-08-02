import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';

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
