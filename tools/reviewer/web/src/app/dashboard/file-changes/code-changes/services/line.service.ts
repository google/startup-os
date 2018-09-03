import { Injectable } from '@angular/core';

import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from '../code-changes.interface';

@Injectable()
export class LineService {
  createEmptyBlockLine(): BlockLine {
    return {
      code: '',
      clearCode: '',
      lineNumber: 0,
      isChanged: false,
      isNewCommentVisible: false,
      isPlaceholder: false,
      comments: [],
    };
  }

  createPlaceholder(): BlockLine {
    const placeholder: BlockLine = this.createEmptyBlockLine();
    placeholder.isPlaceholder = true;
    return placeholder;
  }

  createBlockLine(
    code: string,
    clearCode: string,
    lineNumber: number,
  ): BlockLine {
    const blockLine: BlockLine = this.createEmptyBlockLine();
    blockLine.code = code;
    blockLine.clearCode = clearCode;
    blockLine.lineNumber = lineNumber;
    return blockLine;
  }

  createEmptyChangesLine(): ChangesLine {
    return {
      blocks: [this.createPlaceholder(), this.createPlaceholder()],
      isCommentsLine: false,
      commentsLine: undefined,
    };
  }

  createChangesLine(
    leftBlockLine: BlockLine,
    rightBlockLine: BlockLine,
  ): ChangesLine {
    const changesLine: ChangesLine = this.createEmptyChangesLine();
    changesLine.blocks = [leftBlockLine, rightBlockLine];
    return changesLine;
  }

  createCommentsLine(
    leftBlockLine: BlockLine,
    rightBlockLine: BlockLine,
  ): ChangesLine {
    const commentsLine: ChangesLine = this.createEmptyChangesLine();
    commentsLine.isCommentsLine = true;
    commentsLine.commentsLine = commentsLine;
    commentsLine.blocks[BlockIndex.leftFile].lineNumber = leftBlockLine
      .lineNumber;
    commentsLine.blocks[BlockIndex.rightFile].lineNumber = rightBlockLine
      .lineNumber;
    return commentsLine;
  }
}
