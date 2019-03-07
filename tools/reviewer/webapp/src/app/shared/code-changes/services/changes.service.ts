import { Injectable } from '@angular/core';

import { ChangeType, DiffLine, TextDiff } from '@/core/proto';
import { BlockLine } from '../code-changes.interface';
import { LineService } from './line.service';

// Functions related to lines and TextDiff
@Injectable()
export class ChangesService {
  constructor(private lineService: LineService) { }

  applyTextDiffBlockLines(
    leftBlockLines: BlockLine[],
    rightBlockLines: BlockLine[],
    textDiff: TextDiff,
  ): void {
    this.addPlaceholders(textDiff.getLeftDiffLineList(), leftBlockLines);
    this.addPlaceholders(textDiff.getRightDiffLineList(), rightBlockLines);

    this.applyChanges(textDiff.getLeftDiffLineList(), leftBlockLines);
    this.applyChanges(textDiff.getRightDiffLineList(), rightBlockLines);

    if (leftBlockLines.length !== rightBlockLines.length) {
      throw new Error(
        `After adding all placeholders, blocks should have the same amount of lines.
Left lines: ${leftBlockLines.length}
Right lines: ${rightBlockLines.length}`,
      );
    }
  }

  applyChanges(diffLines: DiffLine[], blockLines: BlockLine[]): void {
    diffLines.forEach(diffLine => {
      switch (diffLine.getType()) {
        case ChangeType.DELETE:
        case ChangeType.ADD:
          // Highlight changes
          blockLines[diffLine.getDiffLineNumber()].isChanged = true;
          blockLines[diffLine.getDiffLineNumber()].diffLine = diffLine;
      }
    });
  }

  addPlaceholders(diffLines: DiffLine[], blockLines: BlockLine[]): void {
    diffLines.forEach(diffLine => {
      if (diffLine.getType() === ChangeType.LINE_PLACEHOLDER) {
        // Add placeholder
        blockLines.splice(
          diffLine.getDiffLineNumber(),
          0,
          this.lineService.createPlaceholder(),
        );
      }
    });
  }
}
