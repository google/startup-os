import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { Thread } from '@/shared/proto';

export interface ThreadState {
  isCommentOpenMap: boolean[];
  isReply: boolean;
  isResolved: boolean;
  newComment: string;
}

interface ThreadStateMap {
  [id: string]: ThreadState;
}

interface ThreadMap {
  [id: string]: Thread;
}

// Service to keep state of thread component, when data is refreshed
@Injectable()
export class ThreadStateService {
  threadMap: ThreadMap = {};
  threadStateMap: ThreadStateMap = {};
  threadChanges = new Subject<void>();
  stateChanges = new Subject<void>();
  openCommentChanges = new Subject<void>();

  // Saves link of new thread in the service
  updateThreadLink(thread: Thread): void {
    this.threadMap[thread.getId()] = thread;
  }

  // Tells thread components that they need to update links
  updateThreadsContent(): void {
    this.threadChanges.next();
  }

  // Tells thread components that they need to update their state.
  // State means "is replying going or not", "text of new comment", "reply checked or not", etc..
  updateState(): void {
    this.stateChanges.next();
  }

  // Tell parent component that a comment is opened.
  // To changed button from "Expand" to "Collapse"
  openComment(): void {
    this.openCommentChanges.next();
  }

  // Resets state
  reset(): void {
    this.threadMap = {};
    this.threadStateMap = {};
  }
}
