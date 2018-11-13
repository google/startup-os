import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService, NotificationService } from '@/core/services';
import { ThreadService } from '../../thread.service';

@Component({
  selector: 'thread-edit-comment',
  templateUrl: './thread-edit-comment.component.html',
  styleUrls: ['./thread-edit-comment.component.scss'],
  providers: [ThreadService],
})
export class ThreadEditCommentComponent implements OnInit {
  textarea = new FormControl();

  @Input() diff: Diff;
  @Input() thread: Thread;
  @Input() comment: Comment;
  @Input() commentIndex: number;
  @Output() closeEmitter = new EventEmitter<void>();

  constructor(
    private diffUpdateService: DiffUpdateService,
    private notificationService: NotificationService,
    private threadService: ThreadService,
  ) { }

  ngOnInit() {
    this.textarea.setValue(this.comment.getContent());
  }

  close(): void {
    this.closeEmitter.emit();
  }

  // Saves comment in firebase
  save(): void {
    const comment: Comment = this.getComment(this.comment.getId());
    if (comment) {
      comment.setContent(this.textarea.value);
      comment.setIsModified(true);
      this.diffUpdateService.saveComment(this.diff);
    }
    this.close();
  }

  // While user was editing a comment, diff could be changed.
  // So we don't change current comment, we take comment from updated diff and change it instead.
  getComment(id: string): Comment {
    const updatedThread: Thread = this.threadService.getThread(this.diff, this.thread);
    if (!updatedThread) {
      this.notificationService.error('Sorry, but the thread is already deleted :(');
      return;
    }

    for (const comment of updatedThread.getCommentList()) {
      if (comment.getId() === id) {
        return comment;
      }
    }

    this.notificationService.error('Sorry, but the comment is already deleted :(');
  }
}
