import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';

import { Comment } from '@/shared';

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
  // A number of the line, where user cursor is hovering right now.
  private hoveredLine: number;
  // Is cursor hovering above new/old code?
  private isHoveredNewCode: boolean;

  // Subjects, which send data:
  lineHeightChanges = new Subject<HeightResponse>();
  openCommentsChanges = new Subject<number>();
  closeCommentsChanges = new Subject<number>();
  newComment = new Subject<AddCommentResponse>();

  // Methods, which receive data:
  setLineHeight(param: HeightResponse): void {
    this.lineHeightChanges.next(param);
  }
  openComments(lineNumber: number): void {
    this.openCommentsChanges.next(lineNumber);
  }
  closeComments(lineNumber: number): void {
    this.closeCommentsChanges.next(lineNumber);
  }
  addComment(param: AddCommentResponse): void {
    this.newComment.next(param);
  }

  // Lines detect mouse hover by the method
  mouseHover(i: number, isNewCode: boolean): void {
    this.hoveredLine = i;
    this.isHoveredNewCode = isNewCode;
  }

  // Lines use it to know, should they display
  // 'add-comment-button` on the line or not
  hoverCheck(i: number, isNewCode: boolean): boolean {
    return this.hoveredLine === i && this.isHoveredNewCode === isNewCode;
  }
}
