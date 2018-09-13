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
    // Are the threads valid?
    this.checkThreadsValidation(
      this.diff.getDiffThreadList(),
      this.diff.getLineThreadList(),
    );

    // threads = lineThreads + diffThreads
    this.threads = this.diff
      .getLineThreadList()
      .concat(this.diff.getDiffThreadList());

    this.sortThreads(this.threads);
  }

  getUnresolvedThreads(): number {
    return this.threads.filter(thread => !thread.getIsDone()).length;
  }

  openFile(thread: Thread): void {
    if (this.isDiffThread(thread)) {
      // We can't open a file of a diff thread
      return;
    }
    this.diffService.openFile(thread.getFile(), this.diff.getId());
  }

  getUsername(comment: Comment): string {
    return this.authService.getUsername(comment.getCreatedBy());
  }

  isDiffThread(thread: Thread): boolean {
    return (thread.getFile() === undefined) ? true : false;
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
    return this.isDiffThread(thread) ? 'diff-thread' : 'line-thread';
  }

  deleteThread(threadIndex: number): void {
    // Remove the thread by its index from thread list
    let diffThreads: Thread[] = this.threads.slice();
    diffThreads.splice(threadIndex, 1);
    diffThreads = diffThreads.filter(thread => this.isDiffThread(thread));
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

  // Each line thread should have a file.
  // Each diff thread shouldn't.
  // We don't have another way to distinguish the threads.
  // Let's check that everything is right.
  checkThreadsValidation(diffThreads: Thread[], lineThreads: Thread[]): void {
    for (const thread of diffThreads) {
      if (thread.getFile() !== undefined) {
        throw new Error('Diff thread is invalid');
      }
    }

    for (const thread of lineThreads) {
      if (thread.getFile() === undefined) {
        throw new Error('Line thread is invalid');
      }
    }
  }
}
