import { Component, Input, OnChanges, OnInit } from '@angular/core';

import {
  TextDiff,
  Thread,
} from '@/core/proto';
import { CommitService } from '../services';
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
    private changesService: ChangesService,
    public hoverService: HoverService,
    public commentsService: CommentsService,
    public templateService: TemplateService,
    private commitService: CommitService,
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

  // Initialize code lines
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

  // Initialize comments
  initComments(threads: Thread[]): void {
    for (const thread of threads) {
      this.startThread(thread);
    }
  }

  // Update comments, when data from firebase is received
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

  // Add the thread to all threads to display it on user screen
  startThread(thread: Thread): void {
    const blockIndex: BlockIndex = this.commitService.getBlockIndex(thread);
    const lineIndex: number = this.changesLinesMap[blockIndex][thread.getLineNumber()];

    // Add the thread to all threads
    this.commentsService.addEmptyThread(
      this.changesLines[lineIndex],
      blockIndex,
    ).thread = thread;

    // Save to threads map for fast access
    this.commentsService.saveAsOpen(
      thread.getLineNumber(),
      lineIndex,
      blockIndex,
    );
  }
}
