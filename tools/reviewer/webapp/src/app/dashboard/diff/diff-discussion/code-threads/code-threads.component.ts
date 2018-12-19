import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';

import { Diff, File, Thread } from '@/core/proto';
import { CommentExpandedMap } from '@/shared/thread';
import { DiffService } from '../../diff.service';
import { DiscussionService } from '../discussion.service';
import { ThreadStateService } from '../thread-state.service';

// The component implements code threads on diff page.
// How it looks: https://i.imgur.com/MobdMJl.jpg
@Component({
  selector: 'code-threads',
  templateUrl: './code-threads.component.html',
  styleUrls: ['./code-threads.component.scss'],
  providers: [DiscussionService],
})
export class CodeThreadsComponent implements OnChanges, OnInit {
  // Threads are divided into groups by filenames
  fileGroupList: Thread[][] = [];
  // Did firebase send any update, when freeze mode was active?
  isQueue: boolean = false;

  @Input() threads: Thread[];
  @Input() diff: Diff;
  @Output() expandEmitter = new EventEmitter<void>();

  constructor(
    private diffService: DiffService,
    public discussionService: DiscussionService,
    public threadStateService: ThreadStateService,
  ) {
    // Update thread, when freeze mode is ended
    this.threadStateService.unfreezeChanges.subscribe(() => {
      if (this.isQueue) {
        this.initThreads();
      }
    });
  }

  ngOnInit() {
    this.initThreads();

  }

  ngOnChanges() {
    if (this.fileGroupList) {
      this.refreshThreads();
    }
  }

  saveStave(thread: Thread, commentExpandedMap: CommentExpandedMap): void {
    this.threadStateService.saveState(thread, commentExpandedMap);
    this.expandEmitter.emit();
  }

  private initThreads(): void {
    this.isQueue = false;
    this.fileGroupList = this.getSortedGroups(this.threads.slice());
    this.threads.forEach(thread => {
      this.threadStateService.createLink(thread);
    });
  }

  private refreshThreads(): void {
    if (!this.threadStateService.getFreezeMode()) {
      this.initThreads();
    } else {
      this.isQueue = true;
    }
  }

  private getSortedGroups(threads: Thread[]): Thread[][] {
    const fileGroups: Thread[][] = this.getFileGroups(threads);
    this.sortGroups(fileGroups);
    return fileGroups;
  }

  // Divide threads into groups by filenames
  private getFileGroups(threads: Thread[]): Thread[][] {
    const fileGroups: { [filename: string]: Thread[] } = {};
    for (const thread of threads) {
      const filename: string = thread.getFile().getFilenameWithRepo();
      if (fileGroups[filename] === undefined) {
        fileGroups[filename] = [];
      }
      fileGroups[filename].push(thread);
    }
    return Object.values(fileGroups);
  }

  // Groups with newest threads go up
  private sortGroups(fileGroups: Thread[][]): void {
    for (const fileGroup of fileGroups) {
      this.discussionService.sortThreads(fileGroup);
    }

    fileGroups.sort((a, b) => this.discussionService.compareLastTimestamps(a[0], b[0]));
  }

  openFile(event: MouseEvent, fileGroup: Thread[]): void {
    // Which button is clicked?
    let isNewTab: boolean;
    switch (event.which) {
      case 1: // Left mouse button
        isNewTab = false;
        break;
      case 2: // Middle mouse button
        isNewTab = true;
        break;
      default: return; // Do nothing, if it's another button
    }

    const groupFile: File = fileGroup[0].getFile();
    this.diffService.openFile(
      isNewTab,
      groupFile,
      this.diff.getId(),
    );
  }

  getFileLabel(threads: Thread[]): string {
    // Example: /project/path/to/file (3 conversations)
    const filename: string = threads[0].getFile().getFilenameWithRepo();
    return filename + ` (${this.discussionService.getConversationLabel(threads.length)})`;
  }
}
