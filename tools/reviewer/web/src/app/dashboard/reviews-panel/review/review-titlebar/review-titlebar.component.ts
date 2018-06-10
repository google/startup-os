import { Diff } from '@/shared/services';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss']
})
export class ReviewTitlebarComponent {
  @Input() diff: Diff;
  @Input() editable;
  @Output() onAddToAttentionList = new EventEmitter<string>();
  // Save needAttentionOf list
  saveAttentionList(name: string): void {
    this.onAddToAttentionList.emit(name);
  }
}
