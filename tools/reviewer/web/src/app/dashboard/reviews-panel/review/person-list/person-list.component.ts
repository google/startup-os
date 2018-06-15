import { Diff } from '@/shared';
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
  persons: string[] = [];

  ngOnChanges() {
    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    switch (this.property) {
      case 'reviewers':
        this.persons = this.diff.getReviewersList();
        break;
      case 'cc':
        this.persons = this.diff.getCcList();
        break;
      default:
        throw new Error('Unsupported property');
    }

    this.propertyValue = this.persons.join(', ');
  }

  // Save the new value of property and update Diff
  savePropertyValue(): void {
    const persons = this.propertyValue.split(',');
    // Trim white spaces

    // WARNING: Bad practice
    // Do not use: object[propertyName]
    // - It forces to use any.
    // - It causes undetected errors.
    // - It's difficult to maintain the code.
    // use instead: object.field
    // TODO: refactor this component ('switch' is not the best way too)

    const newPersonsList = persons.map(v => v.trim()).filter(v => v.length);
    let length;

    switch (this.property) {
      case 'reviewers':
        this.diff.setReviewersList(newPersonsList);
        length = this.diff.getReviewersList().length;
        break;
      case 'cc':
        this.diff.setReviewersList(newPersonsList);
        length = this.diff.getReviewersList().length;
        break;
      default:
        throw new Error('Unsupported property');
    }

    if (length === 0) {
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
    return this.diff.getNeedAttentionOfList().indexOf(person) > -1
      ? 'Remove from Needs Attention'
      : 'Add to Needs Attention';
  }
}
