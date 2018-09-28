import { Injectable } from '@angular/core';

import { BlockLine, BlockIndex } from '../code-changes.interface';

// Functions related mouse cursor and its hovering
@Injectable()
export class HoverService {
  hoveredLineNumber: number;
  hoveredBlockIndex: BlockIndex;
  isLineHovered: boolean = false;

  mouseOver(block: BlockLine, blockIndex: BlockIndex): void {
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

  isHovered(block: BlockLine, blockIndex: BlockIndex): boolean {
    return (
      this.isLineHovered &&
      this.hoveredLineNumber === block.lineNumber &&
      this.hoveredBlockIndex === blockIndex
    );
  }
}
