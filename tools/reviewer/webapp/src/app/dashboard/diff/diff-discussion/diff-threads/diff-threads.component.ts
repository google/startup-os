import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material';

import { Diff, Thread } from '@/shared/proto';
import { DiffUpdateService } from '@/shared/services';
import { DiscussionService } from '../discussion.service';

// The component implements diff threads on diff page.
// How it looks: https://i.imgur.com/fYZn2qn.jpg
@Component({
  selector: 'diff-threads',
  templateUrl: './diff-threads.component.html',
  styleUrls: ['./diff-threads.component.scss'],
  providers: [DiscussionService],
})
export class DiffThreadsComponent implements OnInit, OnChanges {
  displayedColumns = ['discussions'];
  threadsSource: MatTableDataSource<Thread>;

  @Input() threads: Thread[];
  @Input() diff: Diff;

  constructor(
    private diffUpdateService: DiffUpdateService,
    public discussionService: DiscussionService,
  ) { }

  ngOnInit() {
    this.initThreads();
  }

  ngOnChanges() {
   if (this.threadsSource) {
     this.refreshThreads();
   }
  }

  private initThreads(): void {
    const threads: Thread[] = this.threads.slice();
    this.discussionService.sortThreads(threads);
    this.threadsSource = new MatTableDataSource(threads);
  }

  private refreshThreads(): void {
    if (this.threadsSource.data.length === this.threads.length) {
      // Links update
      this.discussionService.refreshThreads(this.threads, Thread.Type.DIFF);
    } else {
      // Re-build template. Each thread component will be recreated.
      this.initThreads();
    }
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
