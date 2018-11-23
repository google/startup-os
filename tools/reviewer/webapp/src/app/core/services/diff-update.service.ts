import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { Diff, Thread } from '@/core/proto';
import { FirebaseService } from './firebase.service';
import { NotificationService } from './notification.service';

@Injectable()
export class DiffUpdateService {
  constructor(
    private router: Router,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  saveComment(diff: Diff): void {
    this.customUpdate(diff, 'Comment is saved in firebase');
  }

  deleteComment(diff: Diff, isDeleteThread: boolean): void {
    if (isDeleteThread) {
      // Delete all threads without comments.
      diff.setCodeThreadList(this.clearEmptyThreads(diff.getCodeThreadList()));
      diff.setDiffThreadList(this.clearEmptyThreads(diff.getDiffThreadList()));
    }

    this.customUpdate(diff, 'Comment is deleted');
  }

  private clearEmptyThreads(threads: Thread[]): Thread[] {
    threads.forEach((thread, threadIndex) => {
      if (thread.getCommentList().length === 0) {
        threads.splice(threadIndex, 1);
      }
    });
    return threads;
  }

  resolveThread(diff: Diff, isChecked: boolean): void {
    const threadStatus: string = isChecked ? 'resolved' : 'unresolved';
    this.customUpdate(diff, 'Thread is ' + threadStatus);
  }

  submitReply(diff: Diff): Observable<void> {
    return new Observable(observer => {
      this.firebaseService.updateDiff(diff).subscribe(() => {
        observer.next();
        this.notificationService.success('Reply submitted');
      });
    });
  }

  updateAttention(diff: Diff, username: string): void {
    const message: string = diff.getAuthor().getNeedsAttention() ?
      `Attention of ${username} is requested` :
      `Attention of ${username} is canceled`;
    this.customUpdate(diff, message);
  }

  updateDescription(diff: Diff): void {
    this.customUpdate(diff, 'Description is saved');
  }

  customUpdate(diff: Diff, message: string): void {
    this.firebaseService.updateDiff(diff).subscribe(() => {
      this.notificationService.success(message);
    });
  }

  deleteDiff(diff: Diff): void {
    this.firebaseService.removeDiff(diff.getId().toString()).subscribe(() => {
      this.notificationService.success('Diff is deleted');
      this.router.navigate(['/diffs']);
    });
  }

  updateIssueList(diff: Diff): void {
    this.customUpdate(diff, 'Issues are updated');
  }
}
