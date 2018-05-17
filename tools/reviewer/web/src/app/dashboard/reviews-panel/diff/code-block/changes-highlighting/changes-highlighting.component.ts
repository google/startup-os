import { Component, Input } from '@angular/core';

import { Line } from '../code-block.component';

@Component({
  selector: 'changes-highlighting',
  templateUrl: './changes-highlighting.component.html',
  styleUrls: ['./changes-highlighting.component.scss'],
})
export class ChangesHighlightingComponent {
  @Input() lines: Line[];
  @Input() isUpdate: boolean;
}
