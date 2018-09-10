import { Injectable } from '@angular/core';

import { BlockLine } from '../code-changes.interface';

// Functions related mouse cursor and its hovering
@Injectable()
export class HoverService {
  hoveredLineNumber: number;
  hoveredBlockIndex: number;
  isLineHovered: boolean = false;

  mouseOver(block: BlockLine, blockIndex: number): void {
    if (block.isPlaceholder) {
      return;
    }
    this.isLineHovered = true;
    this.hoveredLineNumber = block.lineNumber;
    this.hoveredBlockIndex = blockIndex;
  }

  mouseLeave(): void {
    this.isLineHovered = false;
  }

  isHovered(block: BlockLine, blockIndex: number): boolean {
    return (
      this.isLineHovered &&
      this.hoveredLineNumber === block.lineNumber &&
      this.hoveredBlockIndex === blockIndex
    );
  }
}
