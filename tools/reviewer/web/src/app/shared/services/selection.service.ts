import { Injectable } from '@angular/core';

@Injectable()
export class SelectionService {
  clear(): void {
    if (window.getSelection().empty) {  // Chrome
      window.getSelection().empty();
    } else if (window.getSelection().removeAllRanges) {  // Firefox
      window.getSelection().removeAllRanges();
    }
  }
}
