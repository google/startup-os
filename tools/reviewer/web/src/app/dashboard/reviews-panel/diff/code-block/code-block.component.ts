import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';

import { Comment, Thread } from '@/shared';
import { HighlightService } from '@/shared/services';
import { DiffService } from '../diff.service';

export interface Line {
  code: string; // Original code of the line
  highlightedCode: string; // Code with html tags
  isOpen: boolean;  // Does the line have a placeholder for comments?
  isCommentsVisible: boolean; // Should comments be displayed?
  isChanged: boolean; // Should the line be highligted as modified?
  height: number; // Height of placeholder
  comments: Comment[];
}

@Component({
  selector: 'code-block',
  templateUrl: './code-block.component.html',
  styleUrls: ['./code-block.component.scss']
})
export class CodeBlockComponent implements OnInit {
  isComponentInit: boolean = false;
  lines: Line[] = [];
  // Highlighted code with placeholders, in lines.
  highlightedLines: string[] = [];

  @Input() fileContent: string;
  @Input() isUpdate: boolean;
  @Input() changes: number[];
  // TODO: Do not use Threads
  @Input() threads: Thread[];

  constructor(
    private highlightService: HighlightService,
    private changeDetectorRef: ChangeDetectorRef,
    private diffService: DiffService,
  ) {
    this.diffService.lineHeightChanges.subscribe(param => {
      this.lines[param.lineNumber].isOpen = true;
      this.lines[param.lineNumber].height = param.height;
      this.addPlaceholder(param.lineNumber);
    });
    this.diffService.closeCommentsChanges.subscribe(lineNumber => {
      this.closeCommentsBlock(lineNumber);
    });
    this.diffService.openCommentChanges.subscribe(lineNumber => {
      // Open comment always on right side
      if (!this.isUpdate) {
        return;
      }
      this.openCommentsBlock(lineNumber);
    });
  }

  ngOnInit() {
    this.initLines(this.fileContent);
    this.initChanges(this.changes);
    this.initComments(this.threads);
    this.isComponentInit = true;
  }

  ngOnChanges() {
    // Update link to comments, when new thread is created
    if (!this.isComponentInit) {
      return;
    }
    this.initComments(this.threads);
  }

  // Highlitght code, split it by lines.
  // Bootstrap method.
  initLines(fileContent: string): void {
    // TODO: detect language
    const language = 'python';
    const highlightedCode = this.highlightService.highlight(
      fileContent,
      language
    );

    const FileLines = fileContent.split('\n');
    this.highlightedLines = highlightedCode.split('\n');
    FileLines.forEach((code, i) => {
      this.lines.push({
        code: code,
        isOpen: false,
        height: 0,
        highlightedCode: this.highlightedLines[i],
        comments: [],
        isChanged: false,
        isCommentsVisible: false,
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
  // Bootstrap method.
  initComments(threads: Thread[]): void {
    if (!threads) {
      return;
    }
    for (const thread of threads) {
      const i = thread.lineNumber;
      this.lines[i].comments = thread.comments;
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
    if (this.lines[i].isOpen) {
      return;
    }
    this.lines[i].isOpen = true;
    this.lines[i].isCommentsVisible = true;
  }

  closeCommentsBlock(i: number): void {
    this.lines[i].isOpen = false;
    this.lines[i].isCommentsVisible = false;
    this.lines[i].height = 0;
    this.highlightedLines[i] = this.lines[i].highlightedCode;
  }

  // Choose a color of changes highlighting
  lineBackground(line: Line): string {
    if (line.isChanged) {
      return this.isUpdate ? 'new-code' : 'old-code';
    } else {
      return 'default';
    }
  }
}
