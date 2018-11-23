import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'new-thread',
  templateUrl: './new-thread.component.html',
  styleUrls: ['./new-thread.component.scss'],
})
export class NewThreadComponent {
  textarea = new FormControl();

  @Output() closeEmitter = new EventEmitter<void>();
  @Output() addEmitter = new EventEmitter<string>();

  addComment(): void {
    this.addEmitter.emit(this.textarea.value);
  }

  closeThread(): void {
    this.closeEmitter.emit();
  }
}
