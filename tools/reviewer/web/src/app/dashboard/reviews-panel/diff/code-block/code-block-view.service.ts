import { Injectable } from '@angular/core';

import { Line } from './code-block.component';

@Injectable()
export class CodeBlockViewService {
  // Choose a color of changes highlighting
  lineBackground(line: Line, isUpdate: boolean): string {
    if (line.isChanged) {
      return isUpdate ? 'new-code' : 'old-code';
    } else {
      return 'default';
    }
  }
}
