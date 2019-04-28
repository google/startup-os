import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';

import { Diff, Thread } from '@/core/proto';
import { CommentExpandedMap } from '@/shared/thread';
import { DiscussionService } from '../discussion.service';
import { ThreadStateService } from '../thread-state.service';

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
  // Did firebase send any update, when freeze mode was active?
  isQueue: boolean = false;

  @Input() threads: Thread[];
  @Input() diff: Diff;
  // Tells parent that a comment was expanded (to change label of the button)
  @Output() expandEmitter = new EventEmitter<void>();

  constructor(
    public discussionService: DiscussionService,
    public threadStateService: ThreadStateService,
  ) {
    // Update thread, when freeze mode is ended
    this.threadStateService.unfreezeChanges.subscribe(() => {
      if (this.isQueue) {
        this.initThreads();
      }
    });
  }

  ngOnInit() {
    this.initThreads();
  }

  ngOnChanges() {
    if (this.threadList) {
      this.refreshThreads();
    }
  }

  saveStave(thread: Thread, commentExpandedMap: CommentExpandedMap): void {
    this.threadStateService.saveState(thread, commentExpandedMap);
    this.expandEmitter.emit();
  }

  private initThreads(): void {
    this.isQueue = false;
    this.threadList = this.threads.slice();
    this.discussionService.sortThreads(this.threadList);
    this.threads.forEach((thread: Thread) => {
      this.threadStateService.createLink(thread);
    });
  }

  private refreshThreads(): void {
    if (!this.threadStateService.getFreezeMode()) {
      this.initThreads();
    } else {
      this.isQueue = true;
    }
  }
}
