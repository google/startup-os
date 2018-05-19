import { Component, Input } from '@angular/core';

import { DiffService } from '../../diff.service';
import { CodeBlockViewService } from '../code-block-view.service';
import { Line } from '../code-block.component';

// The component implements buttons, which open comments block,
// where a user can add a comment
@Component({
  selector: 'add-comment-buttons',
  templateUrl: './add-comment-buttons.component.html',
  styleUrls: ['./add-comment-buttons.component.scss'],
  providers: [CodeBlockViewService],
})
export class AddCommentButtonsComponent {
  @Input() lines: Line[];
  @Input() isNewCode: boolean;

  constructor(
    public diffService: DiffService,
    public codeBlockViewService: CodeBlockViewService,
  ) { }
}
