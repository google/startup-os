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
    const wordChanges: WordChange[] = blockLine.diffLine.getWordChangeList();

    const className = (blockIndex === BlockIndex.rightFile) ?
      'hl-right' :
      'hl-left';

    // If word changes exist and it's not whole line, then show it
    if (
      wordChanges.length > 1 ||
      wordChanges.length > 0 && (
        wordChanges[0].getStartIndex() !== 0 ||
        wordChanges[0].getEndIndex() !== blockLine.clearCode.length
      )
    ) {
      blockLine.wordChanges = this.makeHighlighting(
        wordChanges,
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
    wordChanges: WordChange[],
    code: string,
    className: string,
  ): string {
    let highlighting: string = '';
    let previousEndIndex: number = 0;

    // Highlight word changes
    wordChanges.forEach((wordChange: WordChange) => {
      // Cut code by indexes
      let codeBeforeWordChange: string = code.substr(
        previousEndIndex,
        wordChange.getStartIndex() - previousEndIndex,
      );
      let codeWithWordChange: string = code.substr(
        wordChange.getStartIndex(),
        wordChange.getEndIndex() - wordChange.getStartIndex(),
      );

      // Escape html special chars
      codeBeforeWordChange = this.highlightService.htmlSpecialChars(codeBeforeWordChange);
      codeWithWordChange = this.highlightService.htmlSpecialChars(codeWithWordChange);

      // Highlight changed part
      const span: HTMLSpanElement = document.createElement('span');
      span.className = className; // Apply color. Red or green.
      span.innerHTML = codeWithWordChange;
      codeWithWordChange = span.outerHTML;

      highlighting += codeBeforeWordChange + codeWithWordChange;
      previousEndIndex = wordChange.getEndIndex();
    });

    // Add code after all word changes
    if (previousEndIndex !== code.length) {
      // Cut code by indexes
      let codeAfterAllChanges: string = code.substr(
        previousEndIndex,
        code.length - previousEndIndex,
      );

      // Escape html special chars
      codeAfterAllChanges = this.highlightService.htmlSpecialChars(codeAfterAllChanges);

      highlighting += codeAfterAllChanges;
    }

    return highlighting;
  }

  // Does the block line contain no comments?
  isEmpty(blockLine: BlockLine): boolean {
    return blockLine.threads.length === 0;
  }

  // Get langulage from filename. Example:
  // filename.js -> javascript
  getLanguage(filename: string): string {
    const extensionRegExp: RegExp = /(?:\.([^.]+))?$/;
    const extension: string = extensionRegExp.exec(filename)[1];

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

      default: return 'clean';
    }

    // All supported languages:
    // https://github.com/highlightjs/highlight.js/tree/master/src/languages
  }
}
