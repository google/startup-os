import { Injectable } from '@angular/core';

import { WordChange } from '@/core/proto';
import { HighlightService } from '@/core/services';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from '../code-changes.interface';

// It's not a great idea to write a lot of typescript code inside html template.
// So most complicated code is located here and is called by template.
@Injectable()
export class TemplateService {
  constructor(private highlightService: HighlightService) { }

  // Get class for a line, based on its parameters
  getLineBackground(
    changesLine: ChangesLine,
    blockIndex: BlockIndex,
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
      return 'common-line';
    }
  }

  // Make line highlighted with separate chars
  highlightChanges(blockLine: BlockLine, blockIndex: BlockIndex): void {
    // TODO: hightlight all word changes (not only first one)
    let startIndex: number = 0;
    let endIndex: number = 0;

    const wordChanges: WordChange[] = blockLine.diffLine.getWordChangeList();
    if (wordChanges.length > 0) {
      startIndex = wordChanges[0].getStartIndex();
      endIndex = wordChanges[0].getEndIndex();
    }

    const className = (blockIndex === BlockIndex.rightFile) ?
      'hl-right' :
      'hl-left';

    if (
      startIndex !== endIndex &&
      startIndex !== 0 ||
      endIndex !== blockLine.clearCode.length
    ) {
      blockLine.wordChanges = this.makeHighlighting(
        startIndex,
        endIndex,
        blockLine.clearCode,
        className,
      );
    }
  }

  // Escape html special chars, and enclose highlighted part in <span> tag
  // example:
  // 'I think x > y here' -> 'I <span>think</span> x &gt; y here'
  // word 'think' is highlighted part
  private makeHighlighting(
    startIndex: number,
    endIndex: number,
    code: string,
    className: string,
  ): string {
    // If the line contains chars changes
    let leftPart: string = code.substr(
      0,
      startIndex,
    );
    let changedPart: string = code.substr(
      startIndex,
      endIndex - startIndex,
    );
    let rightPart: string = code.substr(
      endIndex,
      code.length - endIndex,
    );

    // Escape html special chars
    leftPart = this.highlightService.htmlSpecialChars(leftPart);
    changedPart = this.highlightService.htmlSpecialChars(changedPart);
    rightPart = this.highlightService.htmlSpecialChars(rightPart);

    // Highlight changed part
    const span: HTMLSpanElement = document.createElement('span');
    span.className = className;
    span.innerHTML = changedPart;

    return leftPart + span.outerHTML + rightPart;
  }

  // Does the block line contain no comments?
  isEmpty(blockLine: BlockLine): boolean {
    return blockLine.threads.length === 0;
  }
}
