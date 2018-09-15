import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { Comment, Diff, Thread } from '@/shared/proto';
import { AuthService, FirebaseService, NotificationService } from '@/shared/services';
import { DiffService } from '../diff.service';

// The component implements UI of thread list of the diff
// How it looks: https://i.imgur.com/cc6XITV.jpg

// There's used some methods and interfaces from file-changes
// TODO: make code DRY and use it from one place
@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
  styleUrls: ['./diff-discussion.component.scss'],
  providers: [DiffService],
})
export class DiffDiscussionComponent implements OnInit, OnChanges {
  displayedColumns = ['discussions'];
  threads: Thread[];

  @Input() diff: Diff;

  constructor(
    public diffService: DiffService,
    public authService: AuthService,
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  ngOnInit() {
    this.refreshThreads();
  }

  ngOnChanges() {
    this.refreshThreads();
  }

  refreshThreads(): void {
    // threads = line threads + diff threads
    this.threads = []
      .concat(this.diff.getDiffThreadList())
      .concat(this.diff.getLineThreadList());

    this.sortThreads(this.threads);
  }

  getUnresolvedThreads(): number {
    return this.threads
      .filter(thread => !thread.getIsDone())
      .length;
  }

  openFile(thread: Thread): void {
    if (thread.getIsDiffThread()) {
      // We can't open a file of a diff thread
      return;
    }
    this.diffService.openFile(thread.getFile(), this.diff.getId());
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }

  // Sort all threads based on timestamp of last comment of the thread
  sortThreads(threads: Thread[]): void {
    threads.sort((a, b) => {
      const aLastIndex: number = a.getCommentList().length - 1;
      const bLastIndex: number = b.getCommentList().length - 1;
      const aTimestamp: number = a.getCommentList()[aLastIndex].getTimestamp();
      const bTimestamp: number = b.getCommentList()[bLastIndex].getTimestamp();

      // Newest on top
      return Math.sign(bTimestamp - aTimestamp);
    });
  }

  getThreadBackground(thread: Thread): string {
    return thread.getIsDiffThread() ? 'diff-thread' : 'line-thread';
  }

  deleteThread(threadIndex: number): void {
    // Remove the thread by its index from thread list
    this.threads.splice(threadIndex, 1);
    // Leave diff threads only
    const diffThreads: Thread[] = this.threads
      .filter(thread => thread.getIsDiffThread());
    // Put new diff thread list in diff
    this.diff.setDiffThreadList(diffThreads);

    // Delete from firebase
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Thread is deleted');
    });
  }

  // Make thread resolved / unresolved
  toggleThread(thread: Thread): void {
    // Reverse "isDone" of the thread
    const isDone: boolean = !thread.getIsDone();
    thread.setIsDone(isDone);

    // Save in firebase
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const threadStatus: string = isDone ? 'resolved' : 'unresolved';
      this.notificationService.success('Thread is ' + threadStatus);
    });
  }
}
