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
  // Is cursor hovering above new code?
  private isHoveredNewCode: boolean;

  // Subjects, which send data:
  lineHeightChanges = new Subject<HeightResponse>();
  openCommentsChanges = new Subject<number>();
  closeCommentsChanges = new Subject<number>();
  addCommentChanges = new Subject<AddCommentResponse>();
  deleteCommentChanges = new Subject<boolean>();

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
    this.addCommentChanges.next(param);
  }
  deleteComment(isDeleteThread: boolean): void {
    this.deleteCommentChanges.next(isDeleteThread);
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

  // Get langulage from filename. Example:
  // filename.js -> javascript
  getLanguage(filename: string): string {
    const extensionRegExp: RegExp = /(?:\.([^.]+))?$/;
    const extension: string =  extensionRegExp.exec(filename)[1];

    switch (extension) {
      case 'js': return 'javascript';
      case 'ts': return 'typescript';
      case 'java': return 'java';
      case 'proto': return 'protobuf';
      case 'md': return 'markdown';
      case 'json': return 'json';
      case 'css': return 'css';
      case 'scss': return 'scss';
      case 'html': return 'html';
      case 'sh': return 'bash';
      case 'xml': return 'xml';
      case 'py': return 'python';

      default: return 'code';
    }
  }
}
