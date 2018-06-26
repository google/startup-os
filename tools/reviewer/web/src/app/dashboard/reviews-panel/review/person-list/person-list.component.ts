import { Component, Input, OnInit } from '@angular/core';

import { FirebaseService, NotificationService } from '@/shared/services';
import { Diff, Reviewer } from '@/shared/shell';

// The PersonListComponent is used to display reviewers
@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit {
  title: string;
  @Input() diff: Diff;
  @Input() isNeedAttention: boolean;
  @Input() enableAddToAttention: boolean = true;
  @Input() editable: boolean = true;

  constructor(
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  ngOnInit() {
    this.title = this.isNeedAttention ? 'Reviewers' : 'CC';
  }

  // To show editable fields
  showEditableProperty = false;

  // Following variable is used in editing the fields
  reviewerEmails: string = '';
  reviewers: string[] = [];

  ngOnChanges() {
    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    this.reviewers = this.diff.getReviewerList()
      .filter(reviewer => {
        return reviewer.getNeedsattention() === this.isNeedAttention;
      })
      .map(reviewer => reviewer.getEmail());

    this.reviewerEmails = this.reviewers.join(', ');
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

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Saved');
    }, () => {
      this.notificationService.error('Error');
    });
  }

  // Get text for modifying needAttentionOf field.
  getNeedAttentionText(reviewer: Reviewer): string {
    return reviewer.getNeedsattention() ?
      'Remove from Needs Attention' :
      'Add to Needs Attention';
  }
}
