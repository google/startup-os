import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService,
  Status
} from '@/shared';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit {
  @Input() diff: Diff;
  @Input() property: string;
  @Input() addToAttention: boolean = true;

  // Fields can not be edited if status is
  // 'SUBMITTED' or 'REVERTED'
  editable: boolean = true;
  notEditableStatus: Array<number> = [Status.SUBMITTED, Status.REVERTED];

  // Following variables are used to show editable fields
  showEditableProperty = false;

  // Following variables are used in editing
  // the fields
  reviewers: string = '';
  cc: string = '';

  constructor(
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private notify: NotificationService
  ) {}

  ngOnInit() {}

  ngOnChanges() {
    this.getPropertyValue(this.property);
    // Render the fields un-editable if the current diff status
    // is in the list of notEditableStatus
    this.editable = !this.notEditableStatus.includes(this.diff.status);
  }

  // Get property value from the Diff
  getPropertyValue(property: string): void {
    this[property] = this.diff[property].join(', ');
  }

  // Save the new value of property and update Diff
  savePropertyValue(property: string): void {
    const value = this[property].split(', ');
    this.diff[property] = value.map(v => v.trim());
    this.updateDiff(this.diff, property + ' saved');
  }

  // Save NeetAttentionOf list and update the Diff
  saveAttentionList(name: string): void {
    if (this.diff.needAttentionOf.includes(name)) {
      this.diff.needAttentionOf = this.diff.needAttentionOf.filter(
        e => e !== name
      );
    } else {
      this.diff.needAttentionOf.push(name);
    }
    this.updateDiff(this.diff, 'Need Attention List Updated');
  }

  // Update the Diff in the DB
  updateDiff(diff: Diff, message: string): void {
    this.firebaseService
      .updateDiff(diff)
      .then(res => {
        // TODO make separate service for notifications
        this.notify.success(message);
      })
      .catch(err => {
        this.notify.error('Some Error Occured');
      });
  }

  // Get text for 'Add to Attention List'
  // and 'Remove from Attention List'
  getButtonText(reviewer: string): string {
    return this.diff.needAttentionOf.indexOf(reviewer) > -1
      ? 'Remove From Needs Attention'
      : 'Add to Needs Attention';
  }
}
