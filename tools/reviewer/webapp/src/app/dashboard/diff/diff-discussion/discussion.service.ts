import { Injectable } from '@angular/core';

import { Thread } from '@/core/proto';
import { ThreadStateService } from './thread';

// Methods, which can be reused in code and diff thread components
@Injectable()
export class DiscussionService {
  constructor(private threadStateService: ThreadStateService) { }

  compareLastTimestamps(a: Thread, b: Thread): number {
    const aLastIndex: number = a.getCommentList().length - 1;
    const bLastIndex: number = b.getCommentList().length - 1;
    const aTimestamp: number = a.getCommentList()[aLastIndex].getTimestamp();
    const bTimestamp: number = b.getCommentList()[bLastIndex].getTimestamp();

    // Newest on top
    return Math.sign(bTimestamp - aTimestamp);
  }

  // Sort all threads based on timestamp of last comment of the thread
  sortThreads(threads: Thread[]): void {
    threads.sort((a, b) => this.compareLastTimestamps(a, b));
  }

  // Get text of header of threads
  getHeader(threads: Thread[]): string {
    const conversations: string = (threads.length > 1) ? 'conversations' : 'conversation';
    return `(${threads.length} ${conversations}, ` +
      `${this.getUnresolvedThreads(threads)} unresolved)`;
  }

  private getUnresolvedThreads(threads: Thread[]): number {
    return threads
      .filter(thread => !thread.getIsDone())
      .length;
  }

  // If new thread wasn't added or a thread wasn't deleted then make "links update".
  // Link update means content of each thread will be refreshed, but general threads
  // structure will remain the same.
  refreshThreads(threads: Thread[]): void {
    threads.forEach(thread => {
      this.threadStateService.updateThreadLink(thread);
    });
    this.threadStateService.updateThreadsContent();
  }
}
