import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { ReviewService } from '../review.service';

import {
  AuthService
} from '@/shared/services';
import { Diff } from '@/shared/shell';
import { statusList } from './status-ui';

@Component({
  selector: 'review-titlebar',
  templateUrl: './review-titlebar.component.html',
  styleUrls: ['./review-titlebar.component.scss']
})
export class ReviewTitlebarComponent implements OnInit {
  statusList = statusList;
  isLoading: boolean = true;
  @Input() diff: Diff;
  @Input() editable;

  // Ask review.component to send diff to firebase
  @Output() submit = new EventEmitter();

  constructor(
    public authService: AuthService,
    private reviewService: ReviewService,
  ) { }

  ngOnInit() {
    this.isLoading = false;
  }

  updateAuthor(): void {
    // Update need attention of the author in the diff
    const author = this.diff.getAuthor();
    author.setNeedsattention(!author.getNeedsattention());
    this.diff.setAuthor(author);

    this.reviewService.saveLocalDiff(this.diff);
  }
}
