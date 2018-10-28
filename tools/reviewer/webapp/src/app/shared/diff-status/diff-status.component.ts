import { Component, Input } from '@angular/core';

import { Diff } from '@/core/proto';
import { Status, statusList } from './status-list';

@Component({
  selector: 'diff-status',
  templateUrl: './diff-status.component.html',
  styleUrls: ['./diff-status.component.scss'],
})
export class DiffStatusComponent {
  statusList: Status[] = statusList;
  @Input() diff: Diff;
}
