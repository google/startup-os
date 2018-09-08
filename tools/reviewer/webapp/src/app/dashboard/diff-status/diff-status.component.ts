import { Component, Input } from '@angular/core';

import { Diff } from '@/shared/proto';
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
