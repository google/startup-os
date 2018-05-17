import { Injectable } from '@angular/core';
import { distinctUntilChanged } from 'rxjs/operators';
import { Subject } from 'rxjs/Subject';

import { Comment, Thread } from '@/shared';

interface HeightResponse {
  height: number;
  lineNumber: number;
}

interface AddCommentResponse {
  comments: Comment[];
  lineNumber: number;
}

// The service receives data from one place of
// diff component (and children) and send to another.
@Injectable()
export class DiffService {
  hoveredLine: number;
  isUpdatedHovered: boolean;

  // Subjects, which send data:
  lineHeightChanges = new Subject<HeightResponse>();
  openCommentChanges = new Subject<number>();
  closeCommentsChanges = new Subject<number>();
  newComment = new Subject<AddCommentResponse>();

  // Methods, which receive data:
  setLineHeight(param: HeightResponse): void {
    this.lineHeightChanges.next(param);
  }
  openComments(lineNumber: number): void {
    this.openCommentChanges.next(lineNumber);
  }
  closeComments(lineNumber: number): void {
    this.closeCommentsChanges.next(lineNumber);
  }
  addComment(param: AddCommentResponse): void {
    this.newComment.next(param);
  }

  // Lines detect mouse hover by the method
  mouseHover(i: number, isUpdate: boolean): void {
    this.hoveredLine = i;
    this.isUpdatedHovered = isUpdate;
  }

  // Lines use it to know, should they display
  // 'add-comment-button` on the line or not
  hoverCheck(i: number, isUpdate: boolean): boolean {
    return (
      this.hoveredLine === i &&
      this.isUpdatedHovered === isUpdate
    );
  }
}
