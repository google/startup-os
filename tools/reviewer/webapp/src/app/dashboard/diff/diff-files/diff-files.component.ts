import { Component, Input } from '@angular/core';

import { Diff, File } from '@/core/proto';
import { DiffService } from '../diff.service';

// The component implements UI of file list of the diff
// How it looks: https://i.imgur.com/8vZfGTc.jpg
@Component({
  selector: 'diff-files',
  templateUrl: './diff-files.component.html',
  styleUrls: ['./diff-files.component.scss'],
  providers: [DiffService],
})
export class DiffFilesComponent {
  displayedColumns = ['filename', 'comments', 'modified', 'delta'];
  @Input() diff: Diff;
  @Input() files: File[];
  @Input() diffId: number;

  constructor(public diffService: DiffService) { }

  openFile(event: MouseEvent, file: File): void {
    // Which button is clicked?
    let isNewTab: boolean;
    switch (event.which) {
      case 1: // Left mouse button
        isNewTab = false;
        break;
      case 2: // Middle mouse button
        isNewTab = true;
        break;
      default: return; // Do nothing, if it's another button
    }

    this.diffService.openFile(isNewTab, file, this.diffId);
  }
}
