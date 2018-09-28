import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';

import { StateService } from '../services/state.service';

@Component({
  selector: 'commit-select',
  templateUrl: './commit-select.component.html',
  styleUrls: ['./commit-select.component.scss'],
})
export class CommitSelectComponent implements OnChanges {
  select: string;
  @Input() title: string;
  @Input() commitId: string;
  @Output() selectChanges = new EventEmitter<string>();

  constructor(public stateService: StateService) { }

  // When component receive data
  ngOnChanges() {
    this.select = this.commitId;
  }

  // When select is changed. Time to send data back
  onChange() {
    this.selectChanges.emit(this.select);
  }
}
