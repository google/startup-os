import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { randstr64 } from 'rndmjs';

import { BranchInfo, Comment, Diff, File, TextDiff, Thread } from '@/core/proto';
import { DiffUpdateService, NotificationService, UserService } from '@/core/services';
import {
  BlockIndex,
  BlockLine,
  ChangesLine,
} from './code-changes.interface';
import {
  ChangesService,
  CommentsService,
  Dictionary,
  LineService,
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
    CommentsService,
    ChangesService,
    TemplateService,
    LineService,
  ],
})
export class CodeChangesComponent implements OnInit, OnChanges {
  changesLines: ChangesLine[] = [];
  changesLinesMap: { [id: number]: number }[];
  isComponentInit: boolean = false;
  clickIndex: number = -1;
  // Line indexes of all open threads.
  openThreadsMap: Dictionary[];

  @Input() diff: Diff;
  @Input() branchInfo: BranchInfo;
  @Input() textDiff: TextDiff;
  @Input() threads: Thread[];
  @Input() leftFile: File;
  @Input() rightFile: File;

  constructor(
    private userService: UserService,
    private changesService: ChangesService,
    public commentsService: CommentsService,
    public templateService: TemplateService,
    private notificationService: NotificationService,
    private diffUpdateService: DiffUpdateService,
    private lineService: LineService,
  ) {
    this.openThreadsMap = this.lineService.createSplitDictionary();
  }

  ngOnInit() {
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
    const language: string = this.getLanguage(this.rightFile.getFilename());
    const leftBlockLines: BlockLine[] = this.changesService.getBlockLines(
      this.textDiff.getLeftFileContents(),
      language,
    );
    const rightBlockLines: BlockLine[] = this.changesService.getBlockLines(
      this.textDiff.getRightFileContents(),
      language,
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
    this.openThreadsMap.forEach((lineIndexMap, blockIndex) => {
      for (const lineNumber in lineIndexMap) {
        const lineIndex: number = lineIndexMap[lineNumber];
        const changesLine: ChangesLine = this.changesLines[lineIndex].commentsLine;
        this.commentsService.closeThreads(changesLine, blockIndex, this.openThreadsMap);
      }
    });

    // Open received threads
    this.initComments(threads);
  }

  // Adds the thread to all threads to displays it on user screen
  startThread(thread: Thread): void {
    const blockIndex: BlockIndex = this.getBlockIndex(thread);
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
      const newThread: Thread = this.createNewThread(
        lineNumber,
        thread.getCommentList(),
        this.getFileByBlockIndex(blockIndex),
      );
      this.diff.addCodeThread(newThread);
    }

    this.diffUpdateService.saveComment(this.diff);
  }

  private createNewThread(
    lineNumber: number,
    comments: Comment[],
    file: File,
  ): Thread {
    const newThread: Thread = new Thread();
    newThread.setLineNumber(lineNumber);
    newThread.setIsDone(false);
    newThread.setCommentList(comments);
    newThread.setRepoId(this.branchInfo.getRepoId());
    newThread.setCommitId(file.getCommitId());
    newThread.setFile(file);
    newThread.setType(Thread.Type.CODE);
    newThread.setId(randstr64(6));

    return newThread;
  }

  // Get langulage from filename. Example:
  // filename.js -> javascript
  private getLanguage(filename: string): string {
    const extensionRegExp: RegExp = /(?:\.([^.]+))?$/;
    const extension: string = extensionRegExp.exec(filename)[1];

    switch (extension) {
      case 'js': return 'javascript';
      case 'ts': return 'typescript';
      case 'java': return 'java';
      case 'proto': return 'protobuf';
      case 'md': return 'markdown';
      case 'json': return 'json';
      case 'css': return 'css';
      case 'scss': return 'scss';
      case 'html': return 'html';
      case 'sh': return 'bash';
      case 'xml': return 'xml';
      case 'py': return 'python';

      default: return 'clean';
    }

    // All supported languages:
    // https://github.com/highlightjs/highlight.js/tree/master/src/languages
  }
}
