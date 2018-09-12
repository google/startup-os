import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { Comment, Diff, Thread } from '@/shared/proto';
import { AuthService, FirebaseService, NotificationService } from '@/shared/services';
import { DiffService } from '../diff.service';

// The component implements UI of thread list of the diff
// How it looks: https://i.imgur.com/cc6XITV.jpg

// There's used some methods and interfaces from file-changes
// TODO: make code DRY and reuse it from one place
@Component({
  selector: 'diff-discussion',
  templateUrl: './diff-discussion.component.html',
  styleUrls: ['./diff-discussion.component.scss'],
  providers: [DiffService],
})
export class DiffDiscussionComponent implements OnInit, OnChanges {
  displayedColumns = ['discussions'];
  allThreads: Thread[];

  @Input() diff: Diff;
  @Input() diffId: number;

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
    this.allThreads = this.diff
      .getThreadList()
      .concat(this.diff.getDiffThreadList());
    this.sortThreads(this.allThreads);
  }

  getUnresolvedThreads(): number {
    return this.allThreads.filter(thread => !thread.getIsDone()).length;
  }

  openFile(thread: Thread): void {
    if (this.isDiffThread(thread)) {
      // We can't open diff thread
      return;
    }
    this.diffService.openFile(thread.getFile(), this.diffId);
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }

  isDiffThread(thread: Thread): boolean {
    return (thread.getFile() === undefined) ? true : false;
  }

  // Sort all thread based on timestamp of last comment of the thread
  sortThreads(threads: Thread[]): void {
    threads.sort((a, b) => {
      const aLastIndex: number = a.getCommentList().length - 1;
      const bLastIndex: number = b.getCommentList().length - 1;
      const aTimestamp: number = a.getCommentList()[aLastIndex].getTimestamp();
      const bTimestamp: number = b.getCommentList()[bLastIndex].getTimestamp();

      // Newest on the top
      return Math.sign(bTimestamp - aTimestamp);
    });
  }

  getThreadBackground(thread: Thread): string {
    return this.isDiffThread(thread) ? 'diff-thread' : 'code-thread';
  }

  deleteThread(threadIndex: number): void {
    let diffThreads: Thread[] = this.allThreads.slice();
    diffThreads.splice(threadIndex, 1);
    diffThreads = diffThreads.filter(thread => this.isDiffThread(thread));
    this.diff.setDiffThreadList(diffThreads);
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Thread is deleted');
    });
  }

  // Make thread resolved/unresolved
  toggleThread(thread: Thread): void {
    const isDone: boolean = !thread.getIsDone();
    thread.setIsDone(isDone);
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const threadStatus: string = isDone ? 'resolved' : 'unresolved';
      this.notificationService.success('Thread is ' + threadStatus);
    });
  }
}
