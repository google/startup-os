import { Component, Input } from '@angular/core';

import { CodeBlockViewService } from '../code-block-view.service';
import { Line } from '../code-block.component';

// The component implements line numbers of code block
@Component({
  selector: 'line-numbers',
  templateUrl: './line-numbers.component.html',
  styleUrls: ['./line-numbers.component.scss'],
  providers: [CodeBlockViewService],
})
export class LineNumbersComponent {
  @Input() lines: Line[];
  @Input() isNewCode: boolean;

  constructor(
    public codeBlockViewService: CodeBlockViewService
  ) { }
}
