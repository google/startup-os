import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { Diff, File, Thread } from '@/core/proto';
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
  // Threads are divided into groups by filenames
  fileGroupList: Thread[][] = [];

  @Input() threads: Thread[];
  @Input() diff: Diff;

  constructor(
    private diffService: DiffService,
    public discussionService: DiscussionService,
  ) { }

  ngOnInit() {
    this.initThreads();
  }

  ngOnChanges() {
    if (this.fileGroupList) {
      this.refreshThreads();
    }
  }

  private initThreads(): void {
    this.fileGroupList = this.getSortedGroups(this.threads.slice());
  }

  private refreshThreads(): void {
    if (this.getThreadsAmount(this.fileGroupList) === this.threads.length) {
      // Links update
      this.discussionService.refreshThreads(this.threads);
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

  getFileLabel(threads: Thread[]): string {
    // Example: /project/path/to/file (3 conversations)
    const filename: string = threads[0].getFile().getFilenameWithRepo();
    return filename + ` (${this.discussionService.getConversationLabel(threads.length)})`;
  }
}
