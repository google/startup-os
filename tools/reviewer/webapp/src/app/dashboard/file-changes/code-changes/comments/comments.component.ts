import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Comment } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { FileChangesService } from '../../file-changes.service';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
  LineThread,
} from '../code-changes.interface';
import { CommentsService } from '../services';

// The component implements comments of code changes.
// How it looks: https://i.imgur.com/tVusnEd.jpg
@Component({
  selector: 'line-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss'],
})
export class CommentsComponent {
  textareaControl: FormControl = new FormControl();

  @Input() changesLine: ChangesLine;
  @Input() blockLine: BlockLine;
  @Input() blockIndex: BlockIndex;
  @Input() lineIndex: number;
  @Input() thread: LineThread;

  constructor(
    private commentsService: CommentsService,
    public authService: AuthService,
    private fileChangesService: FileChangesService,
  ) { }

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

    // Send comment to firebase
    this.thread.comments.addComment(comment);
    this.fileChangesService.addComment(
      this.blockLine.lineNumber,
      this.thread.comments.getCommentList(),
    );

    this.textareaControl.reset();

    this.commentsService.saveAsOpen(
      this.blockLine.lineNumber,
      this.lineIndex,
      this.blockIndex,
    );
  }

  deleteComment(index: number): void {
    const comments: Comment[] = this.thread.comments.getCommentList();
    comments.splice(index, 1);
    this.thread.comments.setCommentList(comments);

    // Delete the thread if it doesn't contain comments.
    const isDeleteThread: boolean = this.thread.comments.getCommentList().length === 0;
    this.fileChangesService.deleteComment(isDeleteThread);

    if (isDeleteThread) {
      // Close thread, if it doesn't contain any comments
      this.closeComments();
      this.commentsService.saveAsClosed(
        this.blockLine.lineNumber,
        this.blockIndex,
      );
    }
  }

  closeComments(): void {
    this.commentsService.clearThreads(this.changesLine, this.blockIndex);
  }

  resolveThread(): void {
    this.thread.comments.setIsDone(true);
    this.fileChangesService.resolveThread();
  }
}
