import { Injectable } from '@angular/core';

import { HighlightService } from '@/core/services';
import { BlockIndex, BlockLine, ChangesLine, Dictionary } from '../code-changes.interface';
import { LineService } from './line.service';
import { TemplateService } from './template.service';

// Functions related to left and right blocks
@Injectable()
export class BlocksService {
  constructor(
    private highlightService: HighlightService,
    private lineService: LineService,
    private templateService: TemplateService,
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
    highlightedLines.forEach((lineCode: string, index: number) => {
      const clearLineCode: string = clearCodeLines[index];
      if (clearLineCode === undefined) {
        throw new Error("Highlighted and clear lines don't match");
      }

      blockLines.push(this.lineService.createBlockLine(
        lineCode,
        clearLineCode,
        // index + 1 because we want 1,2,3,4,5... instead of 0,1,2,3,4...
        index + 1,
      ));
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
    const spans: HTMLCollectionOf<HTMLSpanElement> = htmlDocument
      .getElementsByTagName('span') as HTMLCollectionOf<HTMLSpanElement>;
    let multilineSpanList: HTMLSpanElement[] = [];
    Array.from(spans).forEach((span: HTMLSpanElement) => {
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

  // Convert two lists with code (left and right)
  // to one list with code changes
  synchronizeBlockLines(
    leftBlockLines: BlockLine[],
    rightBlockLines: BlockLine[],
    startLineNumber?: number,
    endLineNumber?: number,
  ): {
      changesLines: ChangesLine[];
      changesLinesMap: Dictionary[];
    } {
    const amountOfLines: number = rightBlockLines.length;

    const changesLines: ChangesLine[] = [];
    const changesLinesMap: Dictionary[] = this.lineService.createSplitDictionary();

    startLineNumber = startLineNumber || 0;
    endLineNumber = endLineNumber || amountOfLines;
    for (let i = startLineNumber; i < endLineNumber; i++) {
      // Create line for code
      const codeLine: ChangesLine = this.lineService.createChangesLine(
        leftBlockLines[i],
        rightBlockLines[i],
      );
      this.templateService.highlightChanges(leftBlockLines[i], BlockIndex.leftFile);
      this.templateService.highlightChanges(rightBlockLines[i], BlockIndex.rightFile);

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
}
