import { Comment, Thread } from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cr-thread',
  templateUrl: './thread.component.html',
  styleUrls: ['./thread.component.scss']
})
export class ThreadComponent {
  @Input() thread: Thread;
  @Input() user: string;
  @Output() onUpdateThread = new EventEmitter();
  @Output() removeCurrentThread = new EventEmitter<Thread>();
  constructor() {}

  /**
   * Add comment to DB by pushing the
   * comment into thread.comments
   */
  onAddComment(comment: Comment): void {
    this.thread.comments.push(comment);
    this.onUpdateThread.emit();
  }

  /**
   * Remove the thread
   */
  removeThread(): void {
    this.removeCurrentThread.emit(this.thread);
  }
}
