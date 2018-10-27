import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Comment, Thread } from '@/core/proto';
import { AuthService } from '@/core/services';
import { ThreadService } from '../../services';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from '../code-changes.interface';
import { CommentsService } from '../services';

// The component implements comments of code-changes component.
// How it looks: https://i.imgur.com/tVusnEd.jpg
@Component({
  selector: 'line-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss'],
})
export class CommentsComponent implements OnInit {
  textareaControl: FormControl = new FormControl();
  thread: Thread = new Thread();

  @Input() changesLine: ChangesLine;
  @Input() blockLine: BlockLine;
  @Input() blockIndex: BlockIndex;
  @Input() lineIndex: number;
  @Input() threadIndex: number;

  constructor(
    private commentsService: CommentsService,
    public authService: AuthService,
    private threadService: ThreadService,
  ) { }

  ngOnInit() {
    this.thread = this.blockLine.threadFrames[this.threadIndex].thread;
  }

  addComment(): void {
    if (!this.textareaControl.value) {
      // Blank comments are not allowed.
      return;
    }

    // Create new proto comment
    const comment: Comment = new Comment();
    comment.setContent(this.textareaControl.value);
    comment.setCreatedBy(this.authService.userEmail);
    comment.setTimestamp(Date.now());

    // Add comment and send to firebase
    try {
      this.threadService.addComment(
        this.blockLine.lineNumber,
        comment,
        this.thread,
        this.blockIndex,
      );
    } catch (e) {
      // No need to reset state, if comment wasn't added
      return;
    }

    this.textareaControl.reset();

    this.commentsService.saveAsOpen(
      this.blockLine.lineNumber,
      this.lineIndex,
      this.blockIndex,
    );
  }

  deleteComment(index: number): void {
    // Delete the comment from the thread
    const comments: Comment[] = this.thread.getCommentList();
    comments.splice(index, 1);
    this.thread.setCommentList(comments);

    const isDeleteThread: boolean = this.thread.getCommentList().length === 0;

    // Delete the comment from firebase
    this.threadService.deleteComment(isDeleteThread);

    if (isDeleteThread) {
      // Close the thread, if it doesn't contain any comments
      this.closeThread();
    }
  }

  closeThread(): void {
    this.commentsService.closeThread(
      this.blockLine,
      this.threadIndex,
      this.blockIndex,
    );
  }

  // Make thread resolved / unresolved
  toggleThread(): void {
    // Reverse "isDone" of the thread
    const isDone: boolean = !this.thread.getIsDone();
    this.thread.setIsDone(isDone);

    this.threadService.resolveThread(isDone);
  }
}
