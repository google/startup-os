import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { Diff, Thread } from '@/core/proto';
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
  threadList: Thread[] = [];

  @Input() threads: Thread[];
  @Input() diff: Diff;

  constructor(public discussionService: DiscussionService) { }

  ngOnInit() {
    this.initThreads();
  }

  ngOnChanges() {
   if (this.threadList) {
     this.refreshThreads();
   }
  }

  private initThreads(): void {
    this.threadList = this.threads.slice();
    this.discussionService.sortThreads(this.threadList);
  }

  private refreshThreads(): void {
    if (this.threadList.length === this.threads.length) {
      // Links update
      this.discussionService.refreshThreads(this.threads);
    } else {
      // Re-build template. Each thread component will be recreated.
      this.initThreads();
    }
  }
}
