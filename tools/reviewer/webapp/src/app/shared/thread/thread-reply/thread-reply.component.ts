import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { randstr64 } from 'rndmjs';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService, NotificationService, UserService } from '@/core/services';
import { ThreadService } from '../thread.service';

@Component({
  selector: 'thread-reply',
  templateUrl: './thread-reply.component.html',
  styleUrls: ['./thread-reply.component.scss'],
  providers: [ThreadService],
})
export class ThreadReplyComponent {
  isReply: boolean = false;
  textarea = new FormControl();
  resolvedCheckbox = new FormControl();

  @Input() diff: Diff;
  @Input() thread: Thread;
  @Output() isFreezeModeEmitter = new EventEmitter<boolean>();

  constructor(
    private diffUpdateService: DiffUpdateService,
    private userService: UserService,
    private threadService: ThreadService,
    private notificationService: NotificationService,
  ) { }

  ngOnInit() {
    this.setResolveCheckbox(this.thread.getIsDone());
  }

  // Change state of resolve checkbox.
  // When need to use the method to not call changes subscription.
  setResolveCheckbox(isChecked: boolean): void {
    this.resolvedCheckbox.setValue(isChecked);
  }

  addComment(): void {
    // While user was typing new comment, thread could be changed.
    // So we don't add comment to current thread,
    // we take thread from updated diff and add new comment to it instead.
    const updatedThread: Thread = this.threadService.getThread(this.diff, this.thread);
    if (!updatedThread) {
      this.notificationService.error('Sorry, but the thread is already deleted :(');
      return;
    }

    // Create new comment
    const comment = new Comment();
    comment.setContent(this.textarea.value);
    comment.setCreatedBy(this.userService.email);
    comment.setTimestamp(Date.now());
    comment.setId(randstr64(5));

    // Make the thread resolved/unresolved depends on checkbox
    updatedThread.setIsDone(this.resolvedCheckbox.value);

    // Add the comment to firebase
    updatedThread.addComment(comment);
    this.diffUpdateService.saveComment(this.diff);

    // Refresh state
    this.toggleReply();
  }

  // Open/close reply panel
  toggleReply(): void {
    this.isReply = !this.isReply;
    if (this.isReply) {
      this.setResolveCheckbox(this.thread.getIsDone());
    } else {
      this.textarea.reset();
    }
    this.isFreezeModeEmitter.emit(this.isReply);
  }

  isCodeThread(): boolean {
    return this.thread.getType() === Thread.Type.CODE;
  }
}
