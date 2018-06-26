// There's a bug. Lines with more than 100 chars cross borders of code block.
// TODO: fix the bug
import {
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit
} from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { Comment, Thread } from '@/shared';
import { HighlightService } from '@/shared/services';
import { DiffService } from '../diff.service';

export interface Line {
  code: string; // Original code of the line
  highlightedCode: string; // Code with html tags
  hasPlaceholder: boolean; // Does the line have a placeholder for comments?
  isCommentsVisible: boolean; // Should comments be displayed?
  isChanged: boolean; // Should the line be highligted as modified?
  height: number; // Height of placeholder
  comments: Comment[];
}

// The component implements a single block of a code
// Usually, a diff contains two of them (left and right)
@Component({
  selector: 'code-block',
  templateUrl: './code-block.component.html',
  styleUrls: ['./code-block.component.scss']
})
export class CodeBlockComponent implements OnInit, OnDestroy {
  isComponentInit: boolean = false;
  lines: Line[] = [];
  // Highlighted code with placeholders, in lines.
  highlightedLines: string[] = [];

  @Input() fileContent: string;
  @Input() isNewCode: boolean;
  @Input() changes: number[];
  // TODO: add supporting of deleting whole thread.
  // if you delete a comment in firebase, the comment is deleted on the app too
  // but if you delete a thread in firebase, it's still present
  // on the app until refresh a page.

  // Also notice, if you delete not last comment in a thread, then you need to
  // restore range. e.g. 1,2,3,4... etc.
  // You get an error if there're missed elements. e.g. 1,3,4,6

  // Last thing: if all comments are deleted in a thread, it causes an error.
  // You need to delete a thread, if it doesn't have a comment.
  @Input() threads: Thread[];

  lineHeightChangesSubscription: Subscription;
  closeCommentsChangesSubscription: Subscription;
  openCommentsChangesSubscription: Subscription;

  constructor(
    private highlightService: HighlightService,
    private changeDetectorRef: ChangeDetectorRef,
    private diffService: DiffService
  ) {
    // Subscriptions on events
    this.lineHeightChangesSubscription = this.diffService
      .lineHeightChanges.subscribe(
        param => {
          // Height of a comment block is changed
          this.lines[param.lineNumber].hasPlaceholder = true;
          this.lines[param.lineNumber].height = param.height;
          this.addPlaceholder(param.lineNumber);
        }
      );
    this.closeCommentsChangesSubscription = this.diffService
      .closeCommentsChanges.subscribe(
        lineNumber => {
          // Request for closing a comment block
          this.closeCommentsBlock(lineNumber);
        }
      );
    this.openCommentsChangesSubscription = this.diffService
      .openCommentsChanges.subscribe(
        lineNumber => {
          // Request for opening a comment block
          if (!this.isNewCode) {
            // Open comments always on right side
            return;
          }
          this.openCommentsBlock(lineNumber);
        }
      );
  }

  ngOnInit() {
    this.initLines(this.fileContent);
    this.initChanges(this.changes);
    this.addComments(this.threads);
    this.isComponentInit = true;
  }

  ngOnChanges() {
    // Update link to comments, when new thread is created
    if (!this.isComponentInit) {
      return;
    }
    this.addComments(this.threads);
  }

  ngOnDestroy() {
    this.lineHeightChangesSubscription.unsubscribe();
    this.closeCommentsChangesSubscription.unsubscribe();
    this.openCommentsChangesSubscription.unsubscribe();
  }

  // Highlitght code, split it by lines.
  // Bootstrap method.
  initLines(fileContent: string): void {
    // TODO: detect language
    const language = 'java';
    const highlightedCode = this.highlightService.highlight(
      fileContent,
      language
    );

    const FileLines = fileContent.split('\n');
    this.highlightedLines = highlightedCode.split('\n');
    FileLines.forEach((code, i) => {
      this.lines.push({
        code: code,
        hasPlaceholder: false,
        height: 0,
        highlightedCode: this.highlightedLines[i],
        comments: [],
        isChanged: false,
        isCommentsVisible: false
      });
    });
  }

  // Set which lines are changed
  // Bootstrap method.
  initChanges(changes: number[]): void {
    if (!changes) {
      return;
    }
    for (const lineNumber of changes) {
      this.lines[lineNumber].isChanged = true;
    }
  }

  // Add comments to lines
  addComments(threads: Thread[]): void {
    if (!threads) {
      return;
    }
    for (const thread of threads) {
      const i = thread.getLineNumber();
      this.lines[i].comments = thread.getCommentList();
      this.openCommentsBlock(i);
    }
  }

  // Add a space for comments after the line inside highlighted code
  addPlaceholder(i: number): void {
    const div: HTMLDivElement = document.createElement('div');
    div.style.display = 'inline-block';
    div.style.height = this.lines[i].height + 'px';
    div.innerText = ' ';
    const placeholder = '\n' + div.outerHTML;
    this.highlightedLines[i] = this.lines[i].highlightedCode + placeholder;

    /*
    'What is that?'

    Short answer:
      We need it because we have a dynamic height of the comment placeholders.

    Long answer:
      Comment component counts height of itself and sends it to parent.
      The parent (Code block component) changes own view based on the height
      after the view was checked, but before the component was initialized.
      That causes the error:

      'ERROR Error: ExpressionChangedAfterItHasBeenCheckedError:
      Expression has changed after it was checked.'

      https://stackoverflow.com/a/35243106
      https://stackoverflow.com/a/46605947

      To avoid the issue, we need to detect changes after changing the view.
      Looks like Angular bug.
    */
    if (!this.changeDetectorRef['destroyed']) {
      this.changeDetectorRef.detectChanges();
    }
  }

  openCommentsBlock(i: number): void {
    if (this.lines[i].hasPlaceholder) {
      return;
    }
    this.lines[i].hasPlaceholder = true;
    this.lines[i].isCommentsVisible = true;
  }

  closeCommentsBlock(i: number): void {
    this.lines[i].hasPlaceholder = false;
    this.lines[i].isCommentsVisible = false;
    this.lines[i].height = 0;
    this.highlightedLines[i] = this.lines[i].highlightedCode;
  }
}
