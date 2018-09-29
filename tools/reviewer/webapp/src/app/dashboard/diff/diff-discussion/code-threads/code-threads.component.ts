import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material';

import { Diff, File, Thread } from '@/shared/proto';
import { DiffUpdateService } from '@/shared/services';
import { DiffService } from '../../diff.service';
import { DiscussionService } from '../discussion.service';

// The component implements code threads on diff page.
// How it looks: https://i.imgur.com/MobdMJl.jpg
@Component({
  selector: 'code-threads',
  templateUrl: './code-threads.component.html',
  styleUrls: ['./code-threads.component.scss'],
  providers: [DiscussionService],
})
export class CodeThreadsComponent implements OnInit, OnChanges {
  displayedColumns = ['discussions'];
  // Threads are divided into groups by filenames
  fileGroupsSource: MatTableDataSource<Thread[]>;

  @Input() threads: Thread[];
  @Input() diff: Diff;

  constructor(
    private diffUpdateService: DiffUpdateService,
    private diffService: DiffService,
    public discussionService: DiscussionService,
  ) { }

  ngOnInit() {
    this.initThreads();
  }

  ngOnChanges() {
    if (this.fileGroupsSource) {
      this.refreshThreads();
    }
  }

  private initThreads(): void {
    this.fileGroupsSource = new MatTableDataSource(this.getSortedGroups(this.threads.slice()));
  }

  private refreshThreads(): void {
    if (this.getThreadsAmount(this.fileGroupsSource.data) === this.threads.length) {
      // Links update
      this.discussionService.refreshThreads(this.threads, Thread.Type.CODE);
    } else {
      // Re-build template. Each thread component will be recreated.
      this.initThreads();
    }
  }

  private getSortedGroups(threads: Thread[]): Thread[][] {
    const fileGroups: Thread[][] = this.getFileGroups(threads);
    this.sortGroups(fileGroups);
    return fileGroups;
  }

  // Divide threads into groups by filenames
  private getFileGroups(threads: Thread[]): Thread[][] {
    const fileGroups: { [filename: string]: Thread[] } = {};
    for (const thread of threads) {
      const filename: string = thread.getFile().getFilenameWithRepo();
      if (fileGroups[filename] === undefined) {
        fileGroups[filename] = [];
      }
      fileGroups[filename].push(thread);
    }
    return Object.values(fileGroups);
  }

  // Groups with newest threads go up
  private sortGroups(fileGroups: Thread[][]): void {
    for (const fileGroup of fileGroups) {
      this.discussionService.sortThreads(fileGroup);
    }

    fileGroups.sort((a, b) => this.discussionService.compareLastTimestamps(a[0], b[0]));
  }

  // Get total number of threads in file groups
  private getThreadsAmount(fileGroups: Thread[][]): number {
    let threadsAmount: number = 0;
    fileGroups.forEach(threads => {
      threadsAmount += threads.length;
    });
    return threadsAmount;
  }

  openFile(fileGroup: Thread[]): void {
    const groupFile: File = fileGroup[0].getFile();
    this.diffService.openFile(
      groupFile,
      this.diff.getId(),
    );
  }

  getFilename(threads: Thread[]): string {
    return threads[0].getFile().getFilenameWithRepo();
  }

  addComment(): void {
    this.diffUpdateService.addComment(this.diff);
  }

  resolveThread(isChecked: boolean): void {
    this.diffUpdateService.resolveThread(this.diff, isChecked);
  }

  deleteComment(isDeleteThread: boolean): void {
    this.diffUpdateService.deleteComment(this.diff, isDeleteThread);
  }
}
