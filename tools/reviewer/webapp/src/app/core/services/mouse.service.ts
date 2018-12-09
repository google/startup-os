import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class MouseService {
  mouseup = new Subject<void>();

  constructor() {
    document.onmouseup = (event: MouseEvent) => {
      if (event.which === 1) { // left mouse button
        this.mouseup.next();
      }
    };
  }
}
