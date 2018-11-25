import { Component, Input } from '@angular/core';

@Component({
  selector: 'drag-element',
  templateUrl: './drag-element.component.html',
  styleUrls: ['./drag-element.component.scss'],
})
export class DragElementComponent {
  isPopup: boolean = false;

  @Input() isCircle: boolean;
}
