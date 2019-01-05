import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { randstr64 } from 'rndmjs';

import { BranchInfo, Comment, Diff, File, TextDiff, Thread } from '@/core/proto';
import { DiffUpdateService, NotificationService, UserService } from '@/core/services';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
  CodeGroup,
  Dictionary,
  Section,
} from './code-changes.interface';
import {
  BlocksService,
  ChangesService,
  CommentsService,
  LineService,
  SectionService,
  TemplateService,
} from './services';

const expandAmount: number = 5;

// The component implements code changes.
// How it looks: https://i.imgur.com/HvoXiNC.jpg
// code-changes don't know where changes come from and where it should be sent.
// All the work is on file-changes.
// TODO: add state saving
@Component({
  selector: 'code-changes',
  templateUrl: './code-changes.component.html',
  styleUrls: ['./code-changes.component.scss'],
  providers: [
    CommentsService,
    ChangesService,
    TemplateService,
    LineService,
    BlocksService,
    SectionService,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CodeChangesComponent implements OnInit, OnChanges {
  isComponentInit: boolean = false;
  // Lines divided to groups
  codeGroups: CodeGroup[] = [];
  // Total amount of lines
  amountOfLines: number = 0;
  // Line indexes of all open threads.
  openThreadsMap: Dictionary[];
  // A trick to be able to select code on a single line
  clickIndex: number = -1;

  @Input() diff: Diff;
  @Input() branchInfo: BranchInfo;
  @Input() textDiff: TextDiff;
  @Input() threads: Thread[];
  @Input() leftFile: File;
  @Input() rightFile: File;
  @Input() isShared: boolean;
  @Input() sections: Section[];
  @Output() sectionsEmitter = new EventEmitter<Section[]>();

  constructor(
    private userService: UserService,
    private changesService: ChangesService,
    public commentsService: CommentsService,
    public templateService: TemplateService,
    private notificationService: NotificationService,
    private diffUpdateService: DiffUpdateService,
    private lineService: LineService,
    private blocksService: BlocksService,
    private sectionService: SectionService,
  ) { }

  ngOnInit() {
    this.openThreadsMap = this.lineService.createSplitDictionary();
    this.initLines();
    this.initComments(this.threads);
    this.isComponentInit = true;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.isComponentInit && changes.threads) {
      // New threads are received from firebase
      this.updateComments(this.threads);
    }
  }

  // Initializes code lines
  initLines(): void {
    // Create BlockLines
    const language: string = this.templateService.getLanguage(this.rightFile.getFilename());
    const leftBlockLines: BlockLine[] = this.blocksService.getBlockLines(
      this.textDiff.getLeftFileContents(),
      language,
    );
    const rightBlockLines: BlockLine[] = this.blocksService.getBlockLines(
      this.textDiff.getRightFileContents(),
      language,
    );

    // Add TextDiff to BlockLines
    this.changesService.applyTextDiffBlockLines(
      leftBlockLines,
      rightBlockLines,
      this.textDiff,
    );

    this.amountOfLines = rightBlockLines.length;

    // Divide code to groups
    if (this.isShared) {
      if (!this.sections) {
        // Find changes in code and create groups around it
        this.sections = this.sectionService.getGroupLines(
          this.textDiff.getRightDiffLineList(),
          this.amountOfLines,
        );
      }
    } else {
      // If whole file is opened, we have only 1 group
      this.sections = [{
        startLineNumber: 0,
        endLineNumber: leftBlockLines.length,
      }];
    }

    // Synchronize left and right blocks for each group
    this.codeGroups = [];
    for (const section of this.sections) {
      const {
        changesLines,
        changesLinesMap,
      } = this.blocksService.synchronizeBlockLines(
        leftBlockLines,
        rightBlockLines,
        section.startLineNumber,
        section.endLineNumber,
      );

      this.codeGroups.push({
        changes: changesLines,
        map: changesLinesMap,
        isExpandUpVisible: section.startLineNumber >= 1,
        isExpandDownVisible: section.endLineNumber <= this.amountOfLines - 1,
      });
    }
  }

  expandUp(groupIndex: number): void {
    let lineNumber: number = this.sections[groupIndex].startLineNumber - expandAmount;
    lineNumber = Math.max(lineNumber, 0);
    this.sections[groupIndex].startLineNumber = lineNumber;
    this.refreshSections();
  }

  expandDown(groupIndex: number): void {
    let lineNumber: number = this.sections[groupIndex].endLineNumber + expandAmount;
    lineNumber = Math.min(lineNumber, this.amountOfLines);
    this.sections[groupIndex].endLineNumber = lineNumber;
    this.refreshSections();
  }

  refreshSections(): void {
    this.sections = this.sectionService.getMergedSections(this.sections);
    this.sectionsEmitter.emit(this.sections);
    this.ngOnInit();
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
    this.openThreadsMap.forEach((lineIndexMap, blockIndex) => {
      for (const lineNumber in lineIndexMap) {
        const groupIndex: number = this.sectionService.getGroupIndex(+lineNumber, this.sections);
        const lineIndex: number = lineIndexMap[lineNumber];
        const changesLines: ChangesLine[] = this.codeGroups[groupIndex].changes;
        const changesLine: ChangesLine = changesLines[lineIndex].commentsLine;
        this.commentsService.closeThreads(changesLine, blockIndex, this.openThreadsMap);
      }
    });

    // Open received threads
    this.initComments(threads);
  }

  // Adds the thread to all threads to displays it on user screen
  startThread(thread: Thread): void {
    const groupIndex: number = this.sectionService.getGroupIndex(
      thread.getLineNumber(),
      this.sections,
    );
    if (groupIndex === undefined) {
      // Thread is located out of view
      return;
    }
    const changesLines: ChangesLine[] = this.codeGroups[groupIndex].changes;
    const changesLinesMap: Dictionary[] = this.codeGroups[groupIndex].map;

    const blockIndex: BlockIndex = this.getBlockIndex(thread);
    const lineIndex: number = changesLinesMap[blockIndex][thread.getLineNumber()];
    if (lineIndex === undefined) {
      // Thread is located out of view
      return;
    }

    // Add the thread to all threads
    this.commentsService.addThread(
      changesLines[lineIndex],
      blockIndex,
      thread,
    );

    // Save thread to threads map for fast access
    this.commentsService.saveAsOpen(
      thread.getLineNumber(),
      lineIndex,
      blockIndex,
      this.openThreadsMap,
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
      this.addComment(
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
      this.openThreadsMap,
    );
  }

  // Send diff with new comment to firebase
  private addComment(
    lineNumber: number,
    comment: Comment,
    thread: Thread,
    blockIndex: BlockIndex,
  ): void {
    if (!this.getFileByBlockIndex(blockIndex)) {
      // TODO: Add more UX behavior here
      // For example:
      // Remove button 'add comment' if it's an uncommitted file.
      // Or open uncommitted files in a special readonly mode.
      // etc
      this.notificationService
        .error('Comment cannot be added to an uncommitted file');

      throw new Error('Comment cannot be added to an uncommitted file');
    }

    thread.addComment(comment);
    if (thread.getCommentList().length === 1) {
      // Create new thread
      const newThread: Thread = this.commentsService.createNewThread(
        lineNumber,
        thread.getCommentList(),
        this.getFileByBlockIndex(blockIndex),
        this.branchInfo,
      );
      this.diff.addCodeThread(newThread);
    }

    this.diffUpdateService.saveComment(this.diff);
  }

  private getFileByBlockIndex(blockIndex: BlockIndex): File {
    return [this.leftFile, this.rightFile][blockIndex];
  }

  // Get block index (0 or 1) depends on commit id
  private getBlockIndex(thread: Thread): number {
    for (const blockIndex of [BlockIndex.leftFile, BlockIndex.rightFile]) {
      const commitId: string = this.getFileByBlockIndex(blockIndex).getCommitId();
      if (commitId === thread.getCommitId()) {
        return blockIndex;
      }
    }
  }
}
