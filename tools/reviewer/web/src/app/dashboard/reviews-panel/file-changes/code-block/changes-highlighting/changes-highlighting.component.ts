import { Component, Input } from '@angular/core';

import { Line } from '../code-block.component';

// The component implements highlighting of code changes
@Component({
  selector: 'changes-highlighting',
  templateUrl: './changes-highlighting.component.html',
  styleUrls: ['./changes-highlighting.component.scss'],
})
export class ChangesHighlightingComponent {
  @Input() lines: Line[];
  @Input() isNewCode: boolean;
}
