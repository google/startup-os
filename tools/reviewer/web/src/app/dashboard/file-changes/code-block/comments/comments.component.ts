import {
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Subject } from 'rxjs/Subject';

import { Comment } from '@/shared/proto';
import { AuthService } from '@/shared/services';
import { FileChangesService } from '../../file-changes.service';

// The component implements comments of code block
@Component({
  selector: 'line-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss'],
})
export class CommentsComponent implements OnInit {
  textareaControl: FormControl = new FormControl();
  private componentHeightChanges: Subject<number> = new Subject<number>();
  private onChanges: Subject<number> = new Subject<number>();

  @ViewChild('commentsRef') commentsElementRef: ElementRef;
  @ViewChild('textareaRef') textareaElementRef: ElementRef;
  @Input() lineNumber: number;
  @Input() comments: Comment[];

  constructor(
    private fileChangesService: FileChangesService,
    private authService: AuthService,
  ) {
    // Detect view changes and send height of the component
    this.componentHeightChanges.pipe(distinctUntilChanged()).subscribe(() => {
      this.sendHeight();
    });

    this.onChanges.pipe(debounceTime(10)).subscribe(() => {
      this.sendHeight();
    });

    this.textareaControl.valueChanges.subscribe(() => {
      this.autosize();
      this.sendHeight();
    });
  }

  // Angular hooks
  // https://angular.io/guide/lifecycle-hooks
  ngOnInit() {
    this.autosize();
    this.sendHeight();
  }
  ngAfterContentChecked() {
    this.componentHeightChanges.next(this.getHeight());
  }
  ngOnChanges() {
    this.onChanges.next();
  }

  autosize(): void {
    const textarea: HTMLTextAreaElement = this.textareaElementRef
      .nativeElement;

    textarea.style.overflow = 'hidden';
    textarea.style.height = 'auto';

    let height: number = textarea.scrollHeight;
    height = Math.max(height, 40);
    textarea.style.height = height + 'px';
  }

  getHeight(): number {
    const commentsDiv: HTMLDivElement = this.commentsElementRef.nativeElement;
    return commentsDiv.offsetHeight;
  }

  sendHeight(): void {
    this.fileChangesService.setLineHeight({
      height: this.getHeight(),
      lineNumber: this.lineNumber,
    });
  }

  closeComments(): void {
    this.fileChangesService.closeComments(this.lineNumber);
  }

  addComment(): void {
    if (!this.textareaControl.value) {
      // Blank comments are not allowed.
      return;
    }

    const comment: Comment = new Comment();
    comment.setContent(this.textareaControl.value);
    comment.setCreatedBy(this.authService.userEmail);
    comment.setTimestamp(Date.now());

    this.comments.push(comment);
    this.fileChangesService.addComment({
      comments: this.comments,
      lineNumber: this.lineNumber,
    });

    this.textareaControl.reset();
  }

  deleteComment(index: number): void {
    this.comments.splice(index, 1);

    // Delete the thread if it doesn't contain comments.
    const isDeleteThread: boolean = this.comments.length === 0;
    this.fileChangesService.deleteComment(isDeleteThread);
  }
}
