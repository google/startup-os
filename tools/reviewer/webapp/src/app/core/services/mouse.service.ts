import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class MouseService {
  mousemove = new Subject<MouseEvent>();
  mouseup = new Subject<void>();

  constructor() {
    document.onmousemove = (event: MouseEvent) => {
      this.mousemove.next(event);
    };

    document.onmouseup = (event: MouseEvent) => {
      if (event.which === 1) { // left mouse button
        this.mouseup.next();
      }
    };
  }
}
