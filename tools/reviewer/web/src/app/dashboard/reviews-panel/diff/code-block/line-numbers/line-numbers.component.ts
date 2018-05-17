import { Component, Input } from '@angular/core';

import { CodeBlockViewService } from '../code-block-view.service';
import { Line } from '../code-block.component';

@Component({
  selector: 'line-numbers',
  templateUrl: './line-numbers.component.html',
  styleUrls: ['./line-numbers.component.scss'],
  providers: [CodeBlockViewService],
})
export class LineNumbersComponent {
  @Input() lines: Line[];
  @Input() isUpdate: boolean;

  constructor(
    public codeBlockViewService: CodeBlockViewService
  ) { }
}
