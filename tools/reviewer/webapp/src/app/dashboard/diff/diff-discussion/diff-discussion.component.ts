import { Component, Input, OnChanges, OnInit } from '@angular/core';

import { Comment, Diff, Thread } from '@/shared/proto';
import { AuthService, FirebaseService, NotificationService } from '@/shared/services';
import { DiffService } from '../diff.service';

// To distinguish diff threads and line threads
interface ThreadFrame {
  thread: Thread;
  isDiffThread: boolean;
}

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
  threadFrames: ThreadFrame[];

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
    // line threads + diff threads
    this.threadFrames = [];
    for (const thread of this.diff.getLineThreadList()) {
      this.threadFrames.push({
        thread: thread,
        isDiffThread: false,
      });
    }
    for (const thread of this.diff.getDiffThreadList()) {
      this.threadFrames.push({
        thread: thread,
        isDiffThread: true,
      });
    }

    this.sortThreads(this.threadFrames);
  }

  getUnresolvedThreads(): number {
    return this.threadFrames
      .filter(threadFrame => !threadFrame.thread.getIsDone())
      .length;
  }

  openFile(threadFrame: ThreadFrame): void {
    if (threadFrame.isDiffThread) {
      // We can't open a file of a diff thread
      return;
    }
    this.diffService.openFile(threadFrame.thread.getFile(), this.diff.getId());
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }

  // Sort all threads based on timestamp of last comment of the thread
  sortThreads(ThreadFrames: ThreadFrame[]): void {
    ThreadFrames.sort((aFrame, bFrame) => {
      const a: Thread = aFrame.thread;
      const b: Thread = bFrame.thread;
      const aLastIndex: number = a.getCommentList().length - 1;
      const bLastIndex: number = b.getCommentList().length - 1;
      const aTimestamp: number = a.getCommentList()[aLastIndex].getTimestamp();
      const bTimestamp: number = b.getCommentList()[bLastIndex].getTimestamp();

      // Newest on top
      return Math.sign(bTimestamp - aTimestamp);
    });
  }

  getThreadBackground(threadFrame: ThreadFrame): string {
    return threadFrame.isDiffThread ? 'diff-thread' : 'line-thread';
  }

  deleteThread(threadIndex: number): void {
    // Remove the thread by its index from thread list
    const diffThreadFrames: ThreadFrame[] = this.threadFrames.slice();
    diffThreadFrames.splice(threadIndex, 1);
    const diffThreads: Thread[] = diffThreadFrames
      // Leave diff threads only
      .filter(threadFrame => threadFrame.isDiffThread)
      // Thread frames -> threads
      .map(threadFrame => threadFrame.thread);
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
