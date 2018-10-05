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
  stateChanges = new Subject<Thread.Type>();

  // Save link of new thread in the service
  updateThreadLink(thread: Thread): void {
    this.threadMap[thread.getId()] = thread;
  }

  // Tell thread components that they need to update links
  updateState(type: Thread.Type): void {
    this.stateChanges.next(type);
  }
}
