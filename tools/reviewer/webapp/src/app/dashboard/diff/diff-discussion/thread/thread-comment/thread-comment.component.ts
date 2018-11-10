import { Component, Input, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Subscription } from 'rxjs';

import { Comment, Diff, Thread } from '@/core/proto';
import { DiffUpdateService, UserService } from '@/core/services';
import { DeleteCommentDialogComponent } from '../../delete-comment-dialog';
import { ThreadState, ThreadStateService } from '../thread-state.service';

@Component({
  selector: 'thread-comment',
  templateUrl: './thread-comment.component.html',
  styleUrls: ['./thread-comment.component.scss'],
})
export class ThreadCommentComponent implements OnDestroy {
  isCommentOpenMap: boolean[] = [];
  isCommentEditingMap: boolean[] = [];
  isMenuVisibleMap: boolean[] = [];
  state: ThreadState;
  subscription = new Subscription();

  @Input() diff: Diff;
  @Input() thread: Thread;

  constructor(
    private dialog: MatDialog,
    private userService: UserService,
    private threadStateService: ThreadStateService,
    private diffUpdateService: DiffUpdateService,
  ) {
    // When thread state is changed from outside
    this.subscription = this.threadStateService.stateChanges.subscribe(() => {
      this.getState();
    });
  }

  ngOnInit() {
    this.getState();
  }

  // Updates state after rebuilding template or when state is changed from outside
  getState(): void {
    this.state = this.threadStateService.threadStateMap[this.thread.getId()];
    if (
      this.state &&
      this.state.isCommentOpenMap &&
      this.state.isCommentEditingMap &&
      this.state.isMenuVisibleMap
    ) {
      this.isCommentOpenMap = this.state.isCommentOpenMap;
      this.isCommentEditingMap = this.state.isCommentEditingMap;
      this.isMenuVisibleMap = this.state.isMenuVisibleMap;
    } else {
      // Default values:
      this.initStateMap();
      this.saveState();
    }
  }

  saveState(): void {
    if (!this.state) {
      this.state = this.threadStateService.initThreadState();
    }
    this.state.isCommentOpenMap = this.isCommentOpenMap;
    this.state.isCommentEditingMap = this.isCommentEditingMap;
    this.state.isMenuVisibleMap = this.isMenuVisibleMap;
    this.threadStateService.threadStateMap[this.thread.getId()] = this.state;
  }

  initStateMap(): void {
    this.thread.getCommentList().forEach(() => {
      this.isCommentOpenMap.push(false);
      this.isCommentEditingMap.push(false);
      this.isMenuVisibleMap.push(false);
    });
  }

  // Makes a comment maximized by clicking on it. Full text and date.
  openComment(commentIndex: number): void {
    this.isCommentOpenMap[commentIndex] = true;
    this.saveState();
    this.threadStateService.openComment();
  }

  // Shows/Closes menu to to edit/delete a comment
  toggleMenu(commentIndex: number): void {
    this.isMenuVisibleMap[commentIndex] = !this.isMenuVisibleMap[commentIndex];
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

          // Delete the comment from firebase
          this.diffUpdateService.deleteComment(this.diff, isDeleteThread);
        }
      });
  }

  editComment(commentIndex: number): void {
    this.isCommentEditingMap[commentIndex] = true;
  }

  closeEditing(commentIndex: number): void {
    this.isCommentEditingMap[commentIndex] = false;
  }

  saveMenuState(commentIndex: number, isMenuVisible: boolean): void {
    this.state.isMenuVisibleMap[commentIndex] = isMenuVisible;
    this.saveState();
  }

  getUsername(comment: Comment): string {
    return this.userService.getUsername(comment.getCreatedBy());
  }

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
