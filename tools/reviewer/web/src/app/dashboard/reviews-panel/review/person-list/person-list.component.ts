import { Diff, FirebaseService, NotificationService, Status } from '@/shared';
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
  // Property name in Diff, e.g 'reviewers' or 'cc'.
  // The property value is stored in propertyValue
  @Input() property: string;
  @Input() enableAddToAttention: boolean = true;
  @Input() editable: boolean = true;
  @Output() onUpdateDiff = new EventEmitter<Diff>();
  @Output() onRemoveProperty = new EventEmitter<string>();
  @Output() onAddToAttentionList = new EventEmitter<string>();

  // To show editable fields
  showEditableProperty = false;

  // Following variable is used in editing the fields
  propertyValue: string = '';

  constructor(private firebaseService: FirebaseService) {}

  ngOnChanges() {
    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    this.propertyValue = this.diff[this.property].join(', ');
  }

  // Save the new value of property and update Diff
  savePropertyValue(): void {
    const persons = this.propertyValue.split(',');
    // Trim white spaces
    this.diff[this.property] = persons
      .map(v => v.trim())
      .filter(v => v.length);
    if (this.diff[this.property].length === 0) {
      this.onRemoveProperty.emit(this.property);
    } else {
      this.onUpdateDiff.emit(this.diff);
    }
  }

  // Save needAttentionOf list
  saveAttentionList(name: string): void {
    this.onAddToAttentionList.emit(name);
  }

  // Get text for modifying needAttentionOf field.
  getNeedAttentionText(person: string): string {
    return this.diff.needAttentionOf.indexOf(person) > -1
      ? 'Remove from Needs Attention'
      : 'Add to Needs Attention';
  }
}
