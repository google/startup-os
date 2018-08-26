import { Injectable } from '@angular/core';

import { BlockIndex, BlockLine, ChangesLine } from '../code-changes.interface';

@Injectable()
export class UIService {
  getLineBackground(
    changesLine: ChangesLine,
    blockIndex: number,
    blockLine: BlockLine,
  ): string {
    if (changesLine.isCommentsLine) {
      return 'comments';
    } else if (blockLine.isPlaceholder) {
      return 'placeholder';
    } else if (blockLine.isChanged) {
      switch (blockIndex) {
        case BlockIndex.leftFile: return 'left-file';
        case BlockIndex.rightFile: return 'right-file';
        default:
          throw new Error('Invalid block index');
      }
    } else {
      return 'code-line';
    }
  }
}
