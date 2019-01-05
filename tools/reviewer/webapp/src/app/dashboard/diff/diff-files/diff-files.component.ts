import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Observable } from 'rxjs';

import { Diff, File, Thread } from '@/core/proto';
import { TextDiffReturn, TextDiffService } from '@/core/services';
import { Section } from '@/shared/code-changes';

// The component implements UI of file list of the diff
// How it looks: https://i.imgur.com/8vZfGTc.jpg
@Component({
  selector: 'diff-files',
  templateUrl: './diff-files.component.html',
  styleUrls: ['./diff-files.component.scss'],
})
export class DiffFilesComponent implements OnChanges {
  threads: Thread[];
  isExpanded: boolean = false;
  isCodeLoading: boolean = false;
  changes: TextDiffReturn[];
  // Sections for each file
  sectionsList: Section[][] = [];

  @Input() diff: Diff;
  @Input() files: File[];

  constructor(private textDiffService: TextDiffService) { }

  ngOnChanges(ngChanges: SimpleChanges) {
    if (this.changes && ngChanges.diff) {
      this.checkExpand();
    }
  }

  // Expands or collapses all files
  toggleExpand(): void {
    if (this.isCodeLoading) {
      return;
    }

    this.isExpanded = !this.isExpanded;
    if (!this.isExpanded) {
      this.sectionsList = [];
    }
    this.checkExpand();
  }

  saveSections(sections: Section[], changesIndex: number): void {
    this.sectionsList[changesIndex] = sections;
  }

  // Start changes loading, if files are expanded
  private checkExpand(): void {
    if (this.isExpanded) {
      this.isCodeLoading = true;
      this.loadChanges().subscribe(() => {
        this.isCodeLoading = false;
      });
    }
  }

  // Loads TextDiffReturn from firebase
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
