import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class DocumentEventService {
  mouseup = new Subject<void>();
  keydown = new Subject<KeyboardEvent>();

  constructor() {
    document.onmouseup = (event: MouseEvent) => {
      if (event.button === 0) { // left mouse button
        this.mouseup.next();
      }
    };
    document.onkeydown = (event: KeyboardEvent) => {
      this.keydown.next(event);
    };
  }
}
