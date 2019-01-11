import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Diff, Thread } from '@/core/proto';
import { CommentExpandedMap } from './thread-comments';

// The component implements a single thread.
// How it looks: https://i.imgur.com/WGPp361.jpg
@Component({
  selector: 'cr-thread',
  templateUrl: './thread.component.html',
  styleUrls: ['./thread.component.scss'],
})
export class ThreadComponent {
  // Freeze - it's a system which blocks updating of threads, when user is editing one.
  // We need it, because when two (or more) persons are editing threads, they interfere
  // each other.
  // When one thread is frozen, all threads isn't updating (only for user, which is editing).
  isCommentFreeze: boolean = false;
  isReplyFreeze: boolean = false;

  @Input() thread: Thread;
  @Input() diff: Diff;
  @Input() commentExpandedMap: CommentExpandedMap = {};
  // Tells parent current expand state.
  @Output() saveStateEmitter = new EventEmitter<CommentExpandedMap>();
  // Tells parent status of freeze mode
  @Output() isFreezeModeEmitter = new EventEmitter<boolean>();
  // Tells parent to close the thread
  @Output() closeEmitter = new EventEmitter<void>();
  // Tells parent to add a comment
  @Output() addEmitter = new EventEmitter<string>();

  freezeComments(isFreezeMode: boolean): void {
    this.isCommentFreeze = isFreezeMode;
    this.checkFreeze();
  }

  freezeReply(isFreezeMode: boolean): void {
    this.isReplyFreeze = isFreezeMode;
    this.checkFreeze();
  }

  checkFreeze(): void {
    const isFreezeMode: boolean = this.isCommentFreeze || this.isReplyFreeze;
    this.isFreezeModeEmitter.emit(isFreezeMode);
  }
}
