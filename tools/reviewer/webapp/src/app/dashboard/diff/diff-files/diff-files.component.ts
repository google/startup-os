import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, zip } from 'rxjs';

import { Diff, File, Reviewer, Thread } from '@/core/proto';
import {
  DiffUpdateService,
  NotificationService,
  TextDiffReturn,
  TextDiffService,
  UserService,
} from '@/core/services';
import { Section } from '@/shared/code-changes';

interface ChangeFile {
  file: File;
  isExpanded: boolean;
  checkbox?: FormControl;
  data?: TextDiffReturn;
  sections?: Section[];
}

interface ChangeFileMap {
  [filename: string]: ChangeFile;
}

// The component implements UI of file list of the diff
// How it looks: https://i.imgur.com/8vZfGTc.jpg
@Component({
  selector: 'diff-files',
  templateUrl: './diff-files.component.html',
  styleUrls: ['./diff-files.component.scss'],
})
export class DiffFilesComponent implements OnInit, OnChanges {
  threads: Thread[];
  isExpanded: boolean = false;
  isLoading: boolean = false;
  reviewer: Reviewer;
  changeFileMap: ChangeFileMap = {};

  @Input() diff: Diff;
  @Input() files: File[];

  constructor(
    private textDiffService: TextDiffService,
    private notificationService: NotificationService,
    private userService: UserService,
    private diffUpdateService: DiffUpdateService,
  ) { }

  ngOnInit() {
    this.reviewer = this.userService.getReviewer(this.diff, this.userService.email);
    this.createChangeFileMap();
  }

  ngOnChanges(ngChanges: SimpleChanges) {
    // Update changes when new diff is received from firebase
    if (this.isExpanded && ngChanges.diff) {
      this.loadAllFiles(false);
    }
  }

  // Creates collapsed file list
  private createChangeFileMap(): void {
    this.changeFileMap = {};
    for (const file of this.files) {
      const filename: string = file.getFilenameWithRepo();
      this.changeFileMap[filename] = {
        file: file,
        isExpanded: false,
        checkbox: this.getReviewCheckbox(file),
      };
    }
  }

  private getReviewCheckbox(file: File): FormControl {
    if (this.reviewer) {
      const isFileReviewed: boolean = this.userService.isFileReviewed(this.reviewer, file);
      const checkbox = new FormControl();
      checkbox.setValue(isFileReviewed, { emitEvent: false });

      // When checkbox is clicked
      checkbox.valueChanges.subscribe(checkboxReviewed => {
        this.reviewer = this.userService.getReviewer(this.diff, this.userService.email);
        this.userService.toogleFileReview(checkboxReviewed, this.reviewer, file);
        this.diffUpdateService.reviewFile(this.diff, checkboxReviewed);
      });

      return checkbox;
    }
  }

  getChangeFiles(): ChangeFile[] {
    return Object.values(this.changeFileMap);
  }

  // Is at least one file expanded?
  private getExpand(): boolean {
    let isExpanded: boolean = false;
    for (const changeFile of Object.values(this.changeFileMap)) {
      if (changeFile.isExpanded) {
        isExpanded = true;
        break;
      }
    }
    return isExpanded;
  }

  // Expands or collapses all files
  toggleExpand(): void {
    if (this.isExpanded) {
      for (const changeFile of Object.values(this.changeFileMap)) {
        changeFile.isExpanded = false;
        changeFile.sections = undefined;
      }
      this.isExpanded = false;
    } else {
      this.loadAllFiles(true);
    }
  }

  // Expands or collapses specific file
  toggleFileExpand(changeFile: ChangeFile): void {
    if (!changeFile.isExpanded) {
      if (this.isLoading) {
        return;
      }

      this.isLoading = true;
      this.textDiffService.load(this.diff, changeFile.file.getFilenameWithRepo())
        .subscribe(textDiffReturn => {
          changeFile.data = textDiffReturn;
          changeFile.isExpanded = true;
          this.isExpanded = true;
          this.isLoading = false;
        }, error => {
          if (error.message === 'File not found') {
            this.notificationService.error('Fallback server does not contain the file');
          }
          this.isLoading = false;
        });
    } else {
      changeFile.sections = undefined;
      changeFile.isExpanded = false;
      if (!this.getExpand()) {
        this.isExpanded = false;
      }
    }
  }

  saveSections(sections: Section[], changeFile: ChangeFile): void {
    changeFile.sections = sections;
  }

  private loadAllFiles(expand: boolean): void {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;

    // Create subscribers to synchronize all loadings
    const subscribers = [];
    for (const file of this.files) {
      const subscriber: Observable<TextDiffReturn> = this.textDiffService.load(
        this.diff,
        file.getFilenameWithRepo(),
      );
      subscribers.push(subscriber);
    }

    // Load all files
    zip(...subscribers).subscribe((textDiffReturns: TextDiffReturn[]) => {
      for (const textDiffReturn of textDiffReturns) {
        const filename: string = textDiffReturn.leftFile.getFilenameWithRepo();
        this.changeFileMap[filename] = {
          file: textDiffReturn.rightFile,
          data: textDiffReturn,
          sections: this.changeFileMap[filename].sections,
          isExpanded: expand ? true : this.changeFileMap[filename].isExpanded,
          checkbox: this.getReviewCheckbox(textDiffReturn.rightFile),
        };
      }
      this.isExpanded = true;
      this.isLoading = false;
    }, error => {
      if (error.message === 'File not found') {
        this.notificationService.error(
          'Fallback server does not contain some files from the list',
        );
      }
      this.isLoading = false;
    });
  }
}
