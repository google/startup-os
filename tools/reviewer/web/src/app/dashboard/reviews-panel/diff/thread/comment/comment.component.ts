import { Comment, Thread } from '@/shared';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { mixinColor } from '@angular/material';

@Component({
  selector: 'cr-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.scss']
})
export class CommentComponent implements OnInit {
  @Input() comment: Comment;
  @Input() readonly: boolean = false;
  @Input() placeholder: string = '';
  @Input() user: string;
  @Output() onAddComment = new EventEmitter<Comment>();
  @Output() removeParentThread = new EventEmitter();
  commentContent = '';
  constructor() {}

  ngOnInit() {
    // Initialize the empty comment
    if (this.comment === undefined) {
      this.comment = {
        content: '',
        createdBy: this.user,
        timestamp: new Date().getTime()
      };
    } else {
      this.commentContent = this.comment.content;
    }
  }

  /**
   * Add comment to DB emit comment to parent component
   */
  onAddClicked(): void {
    // TODO validate commentContent
    // Update the timestamp on comment
    if (this.commentContent !== '') {
      this.comment.timestamp = new Date().getTime();
      this.comment.content = this.commentContent;
      this.onAddComment.emit(this.comment);
      this.commentContent = '';
    }
  }

  /**
   * Remove the comment panel/textbox
   */
  removeComment(): void {
    this.commentContent = '';
    this.removeParentThread.emit();
  }
}
