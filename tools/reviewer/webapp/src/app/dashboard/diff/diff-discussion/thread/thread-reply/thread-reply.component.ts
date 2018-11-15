import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService, UserService } from '@/core/services';
import { ThreadState, ThreadStateService } from '../thread-state.service';

@Component({
  selector: 'thread-reply',
  templateUrl: './thread-reply.component.html',
  styleUrls: ['./thread-reply.component.scss'],
})
export class ThreadReplyComponent {
  isReply: boolean = false;
  textarea = new FormControl();
  resolvedCheckbox = new FormControl();
  state: ThreadState;

  @Input() diff: Diff;
  @Input() thread: Thread;

  constructor(
    private threadStateService: ThreadStateService,
    private diffUpdateService: DiffUpdateService,
    private userService: UserService,
  ) {
    // When user is typing new comment
    this.textarea.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.saveState();
      });

    // When resolve checkbox is clicked
    this.resolvedCheckbox.valueChanges.subscribe(() => {
      this.saveState();
    });
  }

  ngOnInit() {
    this.getState();
  }

  // Updates state after rebuilding template
  getState(): void {
    this.state = this.threadStateService.threadStateMap[this.thread.getId()];
    if (
      this.state &&
      this.state.isReply !== undefined &&
      this.state.isResolved !== undefined &&
      this.state.newComment !== undefined
    ) {
      this.isReply = this.state.isReply;
      this.setResolveCheckbox(this.state.isResolved);
      this.textarea.setValue(this.state.newComment, { emitEvent: false });
    } else {
      // Default values:
      this.setResolveCheckbox(this.thread.getIsDone());

      this.saveState();
    }
  }

  saveState(): void {
    if (!this.state) {
      this.state = this.threadStateService.initThreadState();
    }
    this.state.isReply = this.isReply;
    this.state.isResolved = this.resolvedCheckbox.value;
    this.state.newComment = this.textarea.value;
    this.threadStateService.threadStateMap[this.thread.getId()] = this.state;
  }

  // Change state of resolve checkbox.
  // When need to use the method to not call changes subscription.
  setResolveCheckbox(isChecked: boolean): void {
    this.resolvedCheckbox.setValue(isChecked, { emitEvent: false });
  }

  addComment(): void {
    // Create new comment
    const comment = new Comment();
    comment.setContent(this.textarea.value);
    comment.setCreatedBy(this.userService.email);
    comment.setTimestamp(Date.now());

    // Make the thread resolved/unresolved depends on checkbox
    this.thread.setIsDone(this.resolvedCheckbox.value);

    // Add the comment to firebase
    this.thread.addComment(comment);
    this.diffUpdateService.saveComment(this.diff);

    // Refresh state
    this.isReply = false;
    this.textarea.reset();
    this.saveState();
  }

  // Open/close reply panel
  toggleReply(): void {
    this.isReply = !this.isReply;
    if (this.isReply) {
      this.setResolveCheckbox(this.thread.getIsDone());
    } else {
      this.textarea.reset();
    }
    this.saveState();
  }

  isCodeThread(): boolean {
    return this.thread.getType() === Thread.Type.CODE;
  }
}
