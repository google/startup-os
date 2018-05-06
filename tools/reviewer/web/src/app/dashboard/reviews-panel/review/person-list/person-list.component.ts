import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService,
  Status
} from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent {
  @Input() diff: Diff;
  @Input() property: string;
  @Input() addToAttention: boolean = true;
  @Output() onUpdateDiff = new EventEmitter<Diff>();

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
    this.onUpdateDiff.emit(this.diff);
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
    this.onUpdateDiff.emit(this.diff);
  }

  // Get text for 'Add to Attention List'
  // and 'Remove from Attention List'
  getButtonText(reviewer: string): string {
    return this.diff.needAttentionOf.indexOf(reviewer) > -1
      ? 'Remove From Needs Attention'
      : 'Add to Needs Attention';
  }
}
