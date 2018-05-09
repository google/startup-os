import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService,
  Status
} from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

// The PersonListComponent is used to display 'reviewers'
// and 'cc' list of persons

@Component({
  selector: 'app-person-list',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent {
  @Input() diff: Diff;
  @Input() property: string;
  @Input() enableAddToAttention: boolean = true;
  @Input() editable: boolean = true;
  @Output() onUpdateDiff = new EventEmitter<Diff>();

  // Following variables are used to show editable fields
  showEditableProperty = false;

  // Following variables are used in editing the fields
  reviewers: string = '';
  cc: string = '';

  constructor(
    private protoService: ProtoService,
    private firebaseService: FirebaseService
  ) {}

  ngOnChanges() {
    this.getPropertyValue(this.property);
  }

  // Get property value from the Diff
  getPropertyValue(property: string): void {
    this[property] = this.diff[property].join(', ');
  }

  // Save the new value of property and update Diff
  savePropertyValue(property: string): void {
    const persons = this[property].split(', ');
    this.diff[property] = persons.map(v => v.trim());
    this.onUpdateDiff.emit(this.diff);
  }

  // Save needAttentionOf list and update the Diff
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

  // Get text for 'Add to Attention List' and 'Remove from Attention List'
  getButtonText(person: string): string {
    return this.diff.needAttentionOf.indexOf(person) > -1
      ? 'Remove from Needs Attention'
      : 'Add to Needs Attention';
  }
}
