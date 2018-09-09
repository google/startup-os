import { Injectable } from '@angular/core';

import {
  ChangeType,
  TextChange,
  TextDiff,
} from '@/shared/proto';
import { HighlightService } from '@/shared/services';
import {
  BlockIndex, BlockLine, ChangesLine,
} from '../code-changes.interface';
import { LineService } from './line.service';

@Injectable()
export class ChangesService {
  constructor(
    private highlightService: HighlightService,
    private lineService: LineService,
  ) { }

  // Convert string file content to highlighted code lines
  getBlockLines(fileContent: string, language: string): BlockLine[] {
    // Add spans, which highlight the code
    const highlightedCode: string = this.highlightService.highlight(
      fileContent,
      language,
    );

    // Make the spans inline
    const highlightedLines = this
      .makeHighlightingInline(highlightedCode)
      .split('\n');

    // Code lines without highlighting
    const clearCodeLines: string[] = fileContent.split('\n');

    const blockLines: BlockLine[] = [];
    highlightedLines.forEach((lineCode, index) => {
      const clearLineCode: string = clearCodeLines[index];
      if (clearLineCode === undefined) {
        throw new Error("Highlighted and clear lines don't match");
      }

      blockLines.push(
        // index + 1 because we want 1,2,3,4,5... instead of 0,1,2,3,4...
        this.lineService.createBlockLine(lineCode, clearLineCode, index + 1),
      );
    });

    return blockLines;
  }

  makeHighlightingInline(highlightedCode: string): string {
    // Convert html string to DOM object
    const parser: DOMParser = new DOMParser();
    const htmlDocument: Document = parser.parseFromString(
      highlightedCode,
      'text/html',
    );

    // Get spans, which aren't closed on the same line, where they're opened
    const spans: NodeListOf<HTMLSpanElement> = htmlDocument
      .getElementsByTagName('span');
    let multilineSpanList: HTMLSpanElement[] = [];
    Array.from(spans).forEach(span => {
      const innerLines: string[] = span.innerHTML.split('\n');
      if (innerLines.length > 1) {
        multilineSpanList.push(span);
      }
    });
    // Reverse, to make children first
    multilineSpanList = multilineSpanList.reverse();

    // Replace each line of each multiline span with inline span
    for (const multilineSpan of multilineSpanList) {
      const innerLines: string[] = multilineSpan.innerHTML.split('\n');
      const inlineSpanList: string[] = [];
      for (const line of innerLines) {
        const inlineSpan: HTMLSpanElement = document.createElement('span');
        inlineSpan.innerHTML = line;
        inlineSpan.className = multilineSpan.className;
        inlineSpanList.push(inlineSpan.outerHTML);
      }

      // Replace the multiline span with several inline spans
      multilineSpan.outerHTML = inlineSpanList.join('\n');
    }

    return htmlDocument.getElementsByTagName('body')[0].innerHTML;
  }

  // TODO: remove the method, when the issue will be fix
  tempFixChangesLineNumber(textChanges: TextChange[]): void {
    let delimiter: number = 0;
    textChanges.forEach(textChange => {
      switch (textChange.getType()) {
        case ChangeType.DELETE:
        case ChangeType.ADD:
          textChange.setLineNumber(textChange.getLineNumber() - delimiter);
          break;
        case ChangeType.LINE_PLACEHOLDER:
          delimiter++;
      }
    });
  }

  // Convert two lists with code (left and right)
  // to one list with code changes
  synchronizeBlockLines(
    leftBlockLines: BlockLine[],
    rightBlockLines: BlockLine[],
    textDiff: TextDiff,
  ): {
      changesLines: ChangesLine[];
      changesLinesMap: { [id: number]: number }[];
    } {
    this.tempFixChangesLineNumber(textDiff.getLeftChangeList());
    this.tempFixChangesLineNumber(textDiff.getRightChangeList());

    this.applyChanges(textDiff.getLeftChangeList(), leftBlockLines);
    this.applyChanges(textDiff.getRightChangeList(), rightBlockLines);

    this.addPlaceholders(textDiff.getLeftChangeList(), leftBlockLines);
    this.addPlaceholders(textDiff.getRightChangeList(), rightBlockLines);

    if (leftBlockLines.length !== rightBlockLines.length) {
      // After adding all placeholders
      throw new Error('Blocks should have the same amount of lines');
    }
    const amountOfLines: number = leftBlockLines.length;

    const changesLines: ChangesLine[] = [];
    const changesLinesMap: { [id: number]: number }[] = this.lineService
      .createSplitDictionary();
    for (let i = 0; i < amountOfLines; i++) {
      // Create line for code
      const codeLine: ChangesLine = this.lineService.createChangesLine(
        leftBlockLines[i],
        rightBlockLines[i],
      );

      // Add map marker to be able for fast access
      const codeIndex: number = changesLines.length;
      changesLinesMap[BlockIndex.leftFile]
        [leftBlockLines[i].lineNumber] = codeIndex;
      changesLinesMap[BlockIndex.rightFile]
        [rightBlockLines[i].lineNumber] = codeIndex;

      // Create line for comments
      const commentsLine: ChangesLine = this.lineService.createCommentsLine(
        leftBlockLines[i],
        rightBlockLines[i],
      );
      codeLine.commentsLine = commentsLine;

      changesLines.push(codeLine);
      changesLines.push(commentsLine);
    }

    return {
      changesLines: changesLines,
      changesLinesMap: changesLinesMap,
    };
  }

  applyChanges(textChanges: TextChange[], blockLines: BlockLine[]): void {
    textChanges.forEach(textChange => {
      switch (textChange.getType()) {
        case ChangeType.DELETE:
        case ChangeType.ADD:
          // Highlight changes
          blockLines[textChange.getLineNumber()].isChanged = true;
          blockLines[textChange.getLineNumber()].textChange = textChange;
      }
    });
  }

  addPlaceholders(textChanges: TextChange[], blockLines: BlockLine[]): void {
    textChanges.forEach(textChange => {
      if (textChange.getType() === ChangeType.LINE_PLACEHOLDER) {
        // Add placeholder
        blockLines.splice(
          // LineNumber + 1 because we want to add placeholder after the line
          textChange.getLineNumber() + 1,
          0,
          this.lineService.createPlaceholder(),
        );
        // TODO: to use placeholderLineCount ?
      }
    });
  }
}
