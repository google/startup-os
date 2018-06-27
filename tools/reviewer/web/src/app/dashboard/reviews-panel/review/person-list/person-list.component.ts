import { Component, Input, OnInit } from '@angular/core';

import { Diff, Reviewer } from '@/shared/shell';
import { ReviewService } from '../review.service';

// The PersonListComponent is used to display reviewers
@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss'],
})
export class PersonListComponent implements OnInit {
  isNeedAttention: boolean;

  // To show editable fields
  showEditableProperty = false;

  // Following variable is used in editing the fields
  reviewerEmails: string = '';
  reviewers: Reviewer[] = [];

  @Input() diff: Diff;
  @Input() title: string;
  @Input() editable: boolean = true;

  constructor(private reviewService: ReviewService) {
    this.reviewService.diffChanges.subscribe(diff => {
      this.diff = diff;
      this.getPropertyValue();
    });
  }

  ngOnInit() {
    switch (this.title) {
      case 'Reviewers':
        this.isNeedAttention = true;
        break;

      case 'CC':
        this.isNeedAttention = false;
        break;

      default:
        throw new Error('PersonListComponent: unsupported title');
    }

    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    this.reviewers = this.diff.getReviewerList()
      .filter(reviewer => {
        return reviewer.getNeedsattention() === this.isNeedAttention;
      });

    this.reviewerEmails = this.reviewers
      .map(reviewer => reviewer.getEmail())
      .join(', ');
  }

  // Save the new value of property and update Diff
  saveAttentionList(): void {
    let reviewers: Reviewer[] = [];

    this.reviewerEmails
      .split(',')
      .map(reviewer => reviewer.trim())
      .filter(reviewer => reviewer.length)
      .forEach(email => {
        const reviewer = new Reviewer();
        reviewer.setEmail(email);
        reviewer.setApproved(false);
        reviewer.setNeedsattention(this.isNeedAttention);
        reviewers.push(reviewer);
      });

    const otherReviewers = this.diff.getReviewerList().filter(reviewer =>
      reviewer.getNeedsattention() !== this.isNeedAttention
    );

    reviewers = reviewers.concat(otherReviewers);
    this.diff.setReviewerList(reviewers);

    this.reviewService.saveLocalDiff(this.diff);
  }

  // Add to CC, if reviewer is inculed in Reviewers.
  // Add to Reviewers, if reviewer is inculed in CC.
  changeAttentionOfReviewer(reviewer: Reviewer): void {
    reviewer.setNeedsattention(!reviewer.getNeedsattention());
    this.reviewService.saveLocalDiff(this.diff);
  }

  // Get text for modifying needAttentionOf field.
  getNeedAttentionText(reviewer: Reviewer): string {
    return this.isNeedAttention ?
      'Add to CC' :
      'Add to Reviewers';
  }
}
