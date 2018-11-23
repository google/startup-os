import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Subscription } from 'rxjs';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService, UserService } from '@/core/services';
import { DeleteCommentDialogComponent } from '../delete-comment-dialog';
import { CommentExpandedMap } from './thread-comments.interface';

@Component({
  selector: 'thread-comments',
  templateUrl: './thread-comments.component.html',
  styleUrls: ['./thread-comments.component.scss'],
})
export class ThreadCommentsComponent implements OnDestroy {
  isCommentEditingMap: boolean[] = [];
  isMenuVisible: boolean = false;
  subscription = new Subscription();

  @Input() diff: Diff;
  @Input() thread: Thread;
  @Input() commentExpandedMap: CommentExpandedMap;
  @Output() saveStateEmitter = new EventEmitter<CommentExpandedMap>();
  @Output() isFreezeModeEmitter = new EventEmitter<boolean>();
  @Output() closeEmitter = new EventEmitter<void>();

  constructor(
    private dialog: MatDialog,
    private userService: UserService,
    private diffUpdateService: DiffUpdateService,
  ) { }

  ngOnInit() {
    this.initStateMap();
  }

  initStateMap(): void {
    this.thread.getCommentList().forEach(() => {
      this.isCommentEditingMap.push(false);
    });
  }

  // Makes a comment maximized by clicking on it. Full text and date.
  expandComment(comment: Comment): void {
    this.commentExpandedMap[comment.getId()] = true;
    this.saveStateEmitter.emit(this.commentExpandedMap);
  }

  deleteComment(commentIndex: number): void {
    this.dialog.open(DeleteCommentDialogComponent, { width: '380px', autoFocus: false })
      .afterClosed()
      .subscribe((isDelete: boolean) => {
        if (isDelete) {
          // Delete the comment from the thread
          const comments: Comment[] = this.thread.getCommentList();
          comments.splice(commentIndex, 1);
          this.thread.setCommentList(comments);

          const isDeleteThread: boolean = this.thread.getCommentList().length === 0;
          if (isDeleteThread) {
            this.closeEmitter.emit();
          }

          // Delete the comment from firebase
          this.diffUpdateService.deleteComment(this.diff, isDeleteThread);
        }
      });
  }

  // Starts editing of a comment
  editComment(commentIndex: number): void {
    this.isCommentEditingMap[commentIndex] = true;
    this.checkFreeze();
  }

  // Ends editing of a comment
  closeEditing(commentIndex: number): void {
    this.isCommentEditingMap[commentIndex] = false;
    this.checkFreeze();
  }

  // When menu is opened/closed
  saveMenuState(isMenuVisible: boolean): void {
    this.isMenuVisible = isMenuVisible;
    // We need it, because updating of thread can close menu, which we just opened
    this.checkFreeze();
  }

  // Tells parent freeze status of the comments
  checkFreeze(): void {
    if (this.isMenuVisible) {
      this.isFreezeModeEmitter.emit(true);
      return;
    }
    for (const isCommentEditing of this.isCommentEditingMap) {
      if (isCommentEditing) {
        this.isFreezeModeEmitter.emit(true);
        return;
      }
    }
    this.isFreezeModeEmitter.emit(false);
  }

  getUsername(comment: Comment): string {
    return this.userService.getUsername(comment.getCreatedBy());
  }

  // Is comment with the index modified?
  isModified(commentIndex: number): boolean {
    const comment: Comment = this.thread.getCommentList()[commentIndex];
    if (comment) {
      return comment.getIsModified();
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
