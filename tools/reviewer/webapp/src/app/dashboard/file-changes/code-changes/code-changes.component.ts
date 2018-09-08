import { Component, Input, OnChanges, OnInit } from '@angular/core';

import {
  TextDiff,
  Thread,
} from '@/shared/proto';
import { FileChangesService } from '../file-changes.service';
import {
  BlockLine,
  ChangesLine,
} from './code-changes.interface';
import {
  ChangesService,
  CommentsService,
  HoverService,
  LineService,
  TemplateService,
} from './services';

@Component({
  selector: 'code-changes',
  templateUrl: './code-changes.component.html',
  styleUrls: ['./code-changes.component.scss'],
  providers: [
    HoverService,
    ChangesService,
    LineService,
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
    private fileChangesService: FileChangesService,
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

  // Initialize comment threads
  initComments(threads: Thread[]): void {
    for (const thread of threads) {
      this.openComments(thread);
    }
  }

  // Update comments, when data from firebase is received
  updateComments(threads: Thread[]): void {
    // Copy current open threads to create list of empty threads
    const emptyThreads: { [id: number]: number }[] = this.commentsService
      .copyOpenThreads();

    for (const thread of threads) {
      const blockIndex = this.openComments(thread);
      // If thread contains comments then thread isn't empty
      delete emptyThreads[blockIndex][thread.getLineNumber()];
    }

    // Close empty threads
    emptyThreads.forEach((blockEmptyThreads, blockIndex) => {
      for (const lineNumber in blockEmptyThreads) {
        const lineIndex: number = blockEmptyThreads[lineNumber];
        const changesLine: ChangesLine = this.changesLines[lineIndex]
          .commentsLine;
        changesLine.blocks[blockIndex].comments = [];
        this.commentsService.closeComments(changesLine, blockIndex);
      }
    });
  }

  openComments(thread: Thread): number {
    const blockIndex: number = this.fileChangesService.getBlockIndex(thread);
    const lineIndex: number = this.changesLinesMap[blockIndex]
      [thread.getLineNumber()];
    const blockLine: BlockLine = this.changesLines[lineIndex]
      .commentsLine.blocks[blockIndex];
    blockLine.comments = thread.getCommentList();
    this.commentsService.openThread(
      thread.getLineNumber(),
      lineIndex,
      blockIndex,
    );
    this.commentsService.openComments(this.changesLines[lineIndex], 1);

    return blockIndex;
  }
}
