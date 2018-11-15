import { Component, Input, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { Diff, Thread } from '@/core/proto';
import { ThreadStateService } from './thread-state.service';

// The component implements a single thread.
// How it looks: https://i.imgur.com/WGPp361.jpg
@Component({
  selector: 'cr-thread',
  templateUrl: './thread.component.html',
  styleUrls: ['./thread.component.scss'],
})
export class ThreadComponent implements OnDestroy {
  subscription = new Subscription();

  @Input() thread: Thread;
  @Input() diff: Diff;

  constructor(private threadStateService: ThreadStateService) {
    // When firebase sends updated diff
    this.subscription = this.threadStateService.threadChanges.subscribe(() => {
      const thread: Thread = this.threadStateService.threadMap[this.thread.getId()];
      if (thread) {
        this.thread = thread;
      }
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
