import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Comment } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { FileChangesService } from '../../file-changes.service';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from '../code-changes.interface';
import { CommentsService } from '../services';

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
    this.blockLine.comments.push(comment);
    this.fileChangesService.addComment(
      this.blockLine.lineNumber,
      this.blockLine.comments,
    );

    this.textareaControl.reset();

    // Save as opened thread
    this.commentsService.openThread(
      this.blockLine.lineNumber,
      this.lineIndex,
      this.blockIndex,
    );
  }

  deleteComment(index: number): void {
    this.blockLine.comments.splice(index, 1);

    // Delete the thread if it doesn't contain comments.
    const isDeleteThread: boolean = this.blockLine.comments.length === 0;
    this.fileChangesService.deleteComment(isDeleteThread);

    if (isDeleteThread) {
      // Close thread, if it doesn't contain any comments
      this.closeComments();
      this.commentsService.closeThread(
        this.blockLine.lineNumber,
        this.blockIndex,
      );
    }
  }

  closeComments(): void {
    this.commentsService.closeComments(this.changesLine, this.blockIndex);
  }
}
