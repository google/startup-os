import { Component, Input } from '@angular/core';

import { File } from '@/shared/proto';
import { DiffService } from '../diff.service';

@Component({
  selector: 'diff-files',
  templateUrl: './diff-files.component.html',
  styleUrls: ['./diff-files.component.scss'],
  providers: [DiffService],
})
export class DiffFilesComponent {
  displayedColumns = ['filename', 'comments', 'modified', 'delta'];
  @Input() files: File[];
  @Input() diffId: number;

  constructor(public diffService: DiffService) { }

  openFile(file: File) {
    this.diffService.openFile(file, this.diffId);
  }
}
