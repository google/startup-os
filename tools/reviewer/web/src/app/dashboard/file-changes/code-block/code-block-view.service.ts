import { Injectable } from '@angular/core';

import { Line } from './code-block.component';

// View methods of code block component and children,
// which can be reused on several places
@Injectable()
export class CodeBlockViewService {
  // Choose a style of changes highlighting
  lineBackground(line: Line, isNewCode: boolean): string {
    if (line.isChanged) {
      return isNewCode ? 'new-code' : 'old-code';
    } else {
      return 'default';
    }
  }
}
