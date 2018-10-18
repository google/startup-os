import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Comment, Thread } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { DeleteCommentDialogComponent } from '../delete-comment-dialog';
import { ThreadState, ThreadStateService } from './thread-state.service';

// The component implements a single thread.
// How it looks: https://i.imgur.com/WGPp361.jpg
@Component({
  selector: 'cr-thread',
  templateUrl: './thread.component.html',
  styleUrls: ['./thread.component.scss'],
})
export class ThreadComponent implements OnInit, OnDestroy {
  isCommentOpenMap: boolean[] = [];
  isReply: boolean = false;
  textarea = new FormControl();
  resolvedCheckbox = new FormControl();
  subscription = new Subscription();

  @Input() thread: Thread;
  @Output() addCommentEmitter = new EventEmitter<void>();
  @Output() resolveEmitter = new EventEmitter<boolean>();
  @Output() deleteCommentEmitter = new EventEmitter<boolean>();

  constructor(
    private dialog: MatDialog,
    private authService: AuthService,
    private threadStateService: ThreadStateService,
  ) {
    // When firebase sends updated diff
    this.subscription = this.threadStateService.stateChanges.subscribe(type => {
      if (type === this.thread.getType()) {
        this.thread = this.threadStateService.threadMap[this.thread.getId()];
        this.setResolveCheckbox(this.thread.getIsDone());
      }
    });

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

  // Restore state, after rebuilding template
  getState(): void {
    this.setResolveCheckbox(this.thread.getIsDone());

    const state: ThreadState = this.threadStateService.threadStateMap[this.thread.getId()];
    if (state) {
      this.isCommentOpenMap = state.isCommentOpenMap;
      this.isReply = state.isReply;
      this.resolvedCheckbox.setValue(state.isResolved);
      this.textarea.setValue(state.newComment, { emitEvent: false });
    } else {
      this.initCommentOpenMap();
    }
  }

  saveState(): void {
    this.threadStateService.threadStateMap[this.thread.getId()] = {
      isCommentOpenMap: this.isCommentOpenMap,
      isReply: this.isReply,
      isResolved: this.resolvedCheckbox.value,
      newComment: this.textarea.value,
    };
  }

  initCommentOpenMap(): void {
    this.thread.getCommentList().forEach(() => {
      this.isCommentOpenMap.push(false);
    });
  }

  // Change state of resolve checkbox
  setResolveCheckbox(isChecked: boolean): void {
    this.resolvedCheckbox.setValue(this.thread.getIsDone(), { emitEvent: false });
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }

  // Make a comment maximized by clicking on it. Full text and date.
  openComment(commentIndex: number): void {
    this.isCommentOpenMap[commentIndex] = true;
    this.saveState();
  }

  // Open/close reply panel
  toggleReply(): void {
    this.isReply = !this.isReply;
    if (this.isReply) {
      this.resolvedCheckbox.setValue(this.thread.getIsDone());
    } else {
      this.textarea.reset();
    }
    this.saveState();
  }

  isCodeThread(): boolean {
    return this.thread.getType() === Thread.Type.CODE;
  }

  addComment(): void {
    // Create new comment
    const comment = new Comment();
    comment.setContent(this.textarea.value);
    comment.setCreatedBy(this.authService.userEmail);
    comment.setTimestamp(Date.now());

    // Make the thread resolved/unresolved depends on checkbox
    this.thread.setIsDone(this.resolvedCheckbox.value);

    // Add the comment to firebase
    this.thread.addComment(comment);
    this.addCommentEmitter.emit();

    // Refresh state
    this.isReply = false;
    this.textarea.reset();
    this.saveState();
  }

  resolve(isChecked: boolean): void {
    if (isChecked) {
      this.isReply = false;
      this.saveState();
    }
    this.thread.setIsDone(isChecked);
    this.resolveEmitter.emit(isChecked);
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
          this.deleteCommentEmitter.emit(isDeleteThread);
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
