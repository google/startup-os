import {
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Subject } from 'rxjs/Subject';

import { AuthService, Comment } from '@/shared';
import { DiffService } from '../../diff.service';

// The component implements comments of code block
@Component({
  selector: 'line-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.scss']
})
export class CommentsComponent implements OnInit {
  textareaControl = new FormControl();
  private componentHeightChanges = new Subject<number>();
  private onChanges = new Subject<number>();

  @ViewChild('commentsRef') commentsElementRef: ElementRef;
  @ViewChild('textareaRef') textareaElementRef: ElementRef;
  @Input() lineNumber: number;
  @Input() comments: Comment[];

  constructor(
    private diffService: DiffService,
    private authService: AuthService
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
    this.diffService.setLineHeight({
      height: this.getHeight(),
      lineNumber: this.lineNumber
    });
  }

  closeComments(): void {
    this.diffService.closeComments(this.lineNumber);
  }

  addComment(): void {
    const comment = new Comment();
    comment.setContent(this.textareaControl.value);
    comment.setCreatedBy(this.authService.userEmail);
    comment.setTimestamp(Date.now());

    this.comments.push(comment);
    this.diffService.addComment({
      comments: this.comments,
      lineNumber: this.lineNumber
    });

    this.textareaControl.reset();
  }
}
