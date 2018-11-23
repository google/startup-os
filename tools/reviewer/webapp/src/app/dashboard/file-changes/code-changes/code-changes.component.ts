import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { randstr64 } from 'rndmjs';

import { Comment, TextDiff, Thread } from '@/core/proto';
import { UserService } from '@/core/services';
import { CommitService, StateService, ThreadService } from '../services';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from './code-changes.interface';
import {
  ChangesService,
  CommentsService,
  HoverService,
  TemplateService,
} from './services';

// The component implements code changes.
// How it looks: https://i.imgur.com/HvoXiNC.jpg
// code-changes don't know where changes come from and where it should be sent.
// All the work is on file-changes.
@Component({
  selector: 'code-changes',
  templateUrl: './code-changes.component.html',
  styleUrls: ['./code-changes.component.scss'],
  providers: [
    HoverService,
    ChangesService,
    TemplateService,
    ThreadService,
  ],
})
export class CodeChangesComponent implements OnInit, OnChanges {
  changesLines: ChangesLine[] = [];
  changesLinesMap: { [id: number]: number }[];
  isComponentInit: boolean = false;

  @Input() textDiff: TextDiff;
  @Input() language: string;
  @Input() threads: Thread[];

  constructor(
    private userService: UserService,
    private changesService: ChangesService,
    public hoverService: HoverService,
    public commentsService: CommentsService,
    public templateService: TemplateService,
    private commitService: CommitService,
    public stateService: StateService,
    private threadService: ThreadService,
  ) { }

  ngOnInit() {
    this.initLines();
    this.initComments(this.threads);
    this.isComponentInit = true;
  }

  ngOnChanges() {
    if (this.isComponentInit) {
      this.updateComments(this.threads);
    }
  }

  // Initializes code lines
  initLines(): void {
    const leftBlockLines: BlockLine[] = this.changesService.getBlockLines(
      this.textDiff.getLeftFileContents(),
      this.language,
    );
    const rightBlockLines: BlockLine[] = this.changesService.getBlockLines(
      this.textDiff.getRightFileContents(),
      this.language,
    );

    // Synchronize left and right blocks
    const {
      changesLines,
      changesLinesMap,
    } = this.changesService.synchronizeBlockLines(
      leftBlockLines,
      rightBlockLines,
      this.textDiff,
    );

    this.changesLines = changesLines;
    this.changesLinesMap = changesLinesMap;
  }

  // Initializes comments
  initComments(threads: Thread[]): void {
    for (const thread of threads) {
      this.startThread(thread);
    }
  }

  // Updates comments, when data from firebase is received
  updateComments(threads: Thread[]): void {
    // Close open threads
    this.commentsService.openThreadsMap.forEach((lineIndexMap, blockIndex) => {
      for (const lineNumber in lineIndexMap) {
        const lineIndex: number = lineIndexMap[lineNumber];
        const changesLine: ChangesLine = this.changesLines[lineIndex].commentsLine;
        this.commentsService.closeThreads(changesLine, blockIndex);
      }
    });

    // Open received threads
    this.initComments(threads);
  }

  // Adds the thread to all threads to displays it on user screen
  startThread(thread: Thread): void {
    const blockIndex: BlockIndex = this.commitService.getBlockIndex(thread);
    const lineIndex: number = this.changesLinesMap[blockIndex][thread.getLineNumber()];

    // Add the thread to all threads
    this.commentsService.addThread(
      this.changesLines[lineIndex],
      blockIndex,
      thread,
    );

    // Save to threads map for fast access
    this.commentsService.saveAsOpen(
      thread.getLineNumber(),
      lineIndex,
      blockIndex,
    );
  }

  // Creates new thread and sends it to firebase
  addThread(
    value: string,
    blockLine: BlockLine,
    blockIndex: number,
    lineIndex: number,
    thread: Thread,
  ): void {
    if (!value) {
      // Blank comments are not allowed.
      return;
    }

    // Create new proto comment
    const comment: Comment = new Comment();
    comment.setContent(value);
    comment.setCreatedBy(this.userService.email);
    comment.setTimestamp(Date.now());
    comment.setId(randstr64(5));

    // Add comment and send to firebase
    try {
      this.threadService.addComment(
        blockLine.lineNumber,
        comment,
        thread,
        blockIndex,
      );
    } catch (e) {
      // No need to reset state, if comment wasn't added
      return;
    }

    this.commentsService.saveAsOpen(
      blockLine.lineNumber,
      lineIndex,
      blockIndex,
    );
  }
}
