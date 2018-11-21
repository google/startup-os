import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { Thread } from '@/core/proto';
import { CommentExpandedMap } from '@/shared/thread';

interface ThreadExpandedMap {
  [id: string]: CommentExpandedMap;
}

interface FreezeMap {
  [id: string]: boolean;
}

// Service keeps state of threads: which comment expanded and which thread is frozen.
// To read what is freeze mode, please open thread component.
@Injectable()
export class ThreadStateService {
  // Notifies that freeze mode is ended
  unfreezeChanges = new Subject<void>();
  private threadExpandedMap: ThreadExpandedMap = {};
  private freezeMap: FreezeMap = {};

  saveState(thread: Thread, commentExpandedMap: CommentExpandedMap): void {
    this.threadExpandedMap[thread.getId()] = commentExpandedMap;
  }

  getState(thread: Thread): CommentExpandedMap {
    return this.threadExpandedMap[thread.getId()];
  }

  // Makes all comments expanded/collapsed
  toggle(isExpanded: boolean): void {
    for (const threadId in this.threadExpandedMap) {
      const commentExpandedMap: CommentExpandedMap = this.threadExpandedMap[threadId];
      for (const commentId in commentExpandedMap) {
        commentExpandedMap[commentId] = isExpanded;
      }
      this.threadExpandedMap[threadId] = commentExpandedMap;
    }
  }

  // Saves link to comments to be able to expand/collapse them by clicking on the button
  createLink(thread: Thread): void {
    if (!this.threadExpandedMap[thread.getId()]) {
      this.threadExpandedMap[thread.getId()] = {};
    }
    for (const comment of thread.getCommentList()) {
      if (this.threadExpandedMap[thread.getId()][comment.getId()] === undefined) {
        this.threadExpandedMap[thread.getId()][comment.getId()] = false;
      }
    }
  }

  saveFreezeMode(thread: Thread, isFreezeMode: boolean): void {
    this.freezeMap[thread.getId()] = isFreezeMode;
    if (!this.getFreezeMode()) {
      this.unfreezeChanges.next();
    }
  }

  getFreezeMode(): boolean {
    for (const id in this.freezeMap) {
      if (this.freezeMap[id]) {
        return true;
      }
    }

    return false;
  }
}
