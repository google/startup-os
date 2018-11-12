import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService } from '@/core/services';
import { ThreadState, ThreadStateService } from '../../thread-state.service';

@Component({
  selector: 'thread-edit-comment',
  templateUrl: './thread-edit-comment.component.html',
  styleUrls: ['./thread-edit-comment.component.scss'],
})
export class ThreadEditCommentComponent implements OnInit {
  textarea = new FormControl();
  state: ThreadState;

  @Input() diff: Diff;
  @Input() thread: Thread;
  @Input() comment: Comment;
  @Input() commentIndex: number;
  @Output() closeEmitter = new EventEmitter<void>();

  constructor(
    private diffUpdateService: DiffUpdateService,
    private threadStateService: ThreadStateService,
  ) {
    // When user is typing new comment
    this.textarea.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.saveState();
      });
  }

  ngOnInit() {
    this.getState();
  }

  close(): void {
    this.closeEmitter.emit();
  }

  // Saves comment in firebase
  save(): void {
    const comment: Comment = this.comment;
    comment.setContent(this.textarea.value);
    comment.setIsModified(true);
    this.diffUpdateService.saveComment(this.diff);
    this.close();
  }

  // Updates state after rebuilding template
  getState(): void {
    this.state = this.threadStateService.threadStateMap[this.thread.getId()];
    if (
      this.state &&
      this.state.editComments[this.commentIndex] !== undefined
    ) {
      this.textarea.setValue(this.state.editComments[this.commentIndex], { emitEvent: false });
    } else {
      // Default values:
      this.textarea.setValue(this.comment.getContent(), { emitEvent: false });
      this.saveState();
    }
  }

  saveState(): void {
    if (!this.state) {
      this.state = this.threadStateService.initThreadState();
    }
    this.state.editComments[this.commentIndex] = this.textarea.value;
    this.threadStateService.threadStateMap[this.thread.getId()] = this.state;
  }
}
