import { Injectable } from '@angular/core';

import { Thread } from '@/core/proto';

// Methods, which can be reused in code and diff thread components
@Injectable()
export class DiscussionService {
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
    threads.sort((a: Thread, b: Thread) => this.compareLastTimestamps(a, b));
  }

  getConversationLabel(length: number): string {
    // Example: 6 conversations
    const conversations: string = (length > 1) ? 'conversations' : 'conversation';
    return length + ' ' + conversations;
  }

  // Get text of header of threads
  getHeader(threads: Thread[]): string {
    // Example: (6 conversations, 3 unresolved)
    return `(${this.getConversationLabel(threads.length)}, ` +
      `${this.getUnresolvedThreads(threads)} unresolved)`;
  }

  private getUnresolvedThreads(threads: Thread[]): number {
    return threads
      .filter((thread: Thread) => !thread.getIsDone())
      .length;
  }
}
