import { Diff } from '@/shared/services';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss']
})
export class ReviewTitlebarComponent implements OnInit {
  @Input() diff: Diff;
  @Input() editable;
  @Output() onAddToAttentionList = new EventEmitter<string>();

  constructor() {}

  ngOnInit() {}

  // Save needAttentionOf list
  saveAttentionList(name: string): void {
    this.onAddToAttentionList.emit(name);
  }
}
