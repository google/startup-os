import { Component, Input } from '@angular/core';
import { Observable } from 'rxjs';

import { Diff, File, TextDiff, Thread } from '@/core/proto';
import { TextDiffReturn, TextDiffService } from '@/core/services';

// The component implements UI of file list of the diff
// How it looks: https://i.imgur.com/8vZfGTc.jpg
@Component({
  selector: 'diff-files',
  templateUrl: './diff-files.component.html',
  styleUrls: ['./diff-files.component.scss'],
})
export class DiffFilesComponent {
  textDiff: TextDiff;
  language: string;
  threads: Thread[];
  isExpanded: boolean = false;
  isCodeLoading: boolean = false;
  changes: TextDiffReturn[];

  @Input() diff: Diff;
  @Input() files: File[];
  @Input() diffId: number;

  constructor(private textDiffService: TextDiffService) { }

  toggleExpand(): void {
    if (this.isCodeLoading) {
      return;
    }

    this.isExpanded = !this.isExpanded;
    if (this.isExpanded) {
      this.isCodeLoading = true;
      this.loadChanges().subscribe(() => {
        this.isCodeLoading = false;
      });
    }
  }

  private loadChanges(): Observable<void> {
    return new Observable(observer => {
      const subscribers = [];
      for (const file of this.files) {
        const subscriber: Observable<TextDiffReturn> = this.textDiffService.load(
          this.diff,
          file.getFilenameWithRepo(),
        );
        subscribers.push(subscriber);
      }

      Observable
        .zip(...subscribers)
        .subscribe((textDiffReturns: TextDiffReturn[]) => {
          this.changes = textDiffReturns;
          observer.next();
        });
    });
  }
}
