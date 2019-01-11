import { Injectable } from '@angular/core';

import { Diff, Thread } from '@/core/proto';

@Injectable()
export class ThreadService {
  // Searches a thread in a diff by thread id
  getThread(diff: Diff, thread: Thread): Thread {
    const diffThread: Thread = this.getThreadFromList(diff.getDiffThreadList(), thread);
    if (diffThread) {
      return diffThread;
    }
    const codeThread: Thread = this.getThreadFromList(diff.getCodeThreadList(), thread);
    if (codeThread) {
      return codeThread;
    }
  }

  // Searches a thread in thread list by thread id
  private getThreadFromList(threadList: Thread[], targetThread: Thread): Thread {
    for (const thread of threadList) {
      if (thread.getId() === targetThread.getId()) {
        return thread;
      }
    }
  }
}
