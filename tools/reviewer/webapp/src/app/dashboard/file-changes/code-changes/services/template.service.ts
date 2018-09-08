import { Injectable } from '@angular/core';

import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from '../code-changes.interface';

@Injectable()
export class TemplateService {
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

  // Replace html special chars with html entities
  htmlSpecialChars(code: string): string {
    const findSpecialChars: RegExp = /[&<>"'`=\/]/g;
    return code.replace(findSpecialChars, char => {
      switch (char) {
        case '&': return '&amp;';
        case '<': return '&lt;';
        case '>': return '&gt;';
        case '"': return '&quot;';
        case "'": return '&#39;';
        case '/': return '&#x2F;';
        case '`': return '&#x60;';
        case '=': return '&#x3D;';
        default: return char;
      }
    });
  }

  // Make line highlighted with separate chars
  highlightChanges(blockLine: BlockLine, blockIndex: number): string {
    const startIndex: number = blockLine.textChange.getStartIndex();
    const endIndex: number = blockLine.textChange.getEndIndex();
    const className = (blockIndex === BlockIndex.rightFile) ?
      'hl-right' :
      'hl-left';

    if (
      startIndex === endIndex ||
      (startIndex === 0 && endIndex === blockLine.clearCode.length)
    ) {
      // No need to highlight chars, if whole line was changed.
      return blockLine.clearCode;
    } else {
      return this.makeHighlighting(
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
    leftPart = this.htmlSpecialChars(leftPart);
    changedPart = this.htmlSpecialChars(changedPart);
    rightPart = this.htmlSpecialChars(rightPart);

    // Highlight changed part
    const span: HTMLSpanElement = document.createElement('span');
    span.className = className;
    span.innerHTML = changedPart;

    return leftPart + span.outerHTML + rightPart;
  }
}
