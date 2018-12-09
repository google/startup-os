import { Component, Input } from '@angular/core';

export interface CommitInfo {
  id: string;
  timestamp: number;
  offset: number;
  isVisible: boolean;
  isInit: boolean;
}

@Component({
  selector: 'commit-popup',
  templateUrl: './commit-popup.component.html',
  styleUrls: ['./commit-popup.component.scss'],
})
export class CommitPopupComponent {
  @Input() commitInfo: CommitInfo;

  getId(id: string): string {
    return id || 'Uncommited';
  }
}
