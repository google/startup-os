import { Component, Input } from '@angular/core';

import { FirebaseService, NotificationService } from '@/shared/services';
import { Diff } from '@/shared/shell';

@Component({
  selector: 'app-editable-property',
  templateUrl: './editable-property.component.html',
  styleUrls: ['./editable-property.component.scss']
}
)export class EditablePropertyComponent {
  @Input() diff: Diff;
  // Property name in Diff, e.g 'bug' or 'description'.
  // The property value is stored in propertyValue
  @Input() property: string;
  @Input() editable: boolean = true;

  // To show editable fields
  showEditableProperty = false;

  // Following variable is used in editing the fields
  propertyValue: string = '';

  constructor(
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
  ) { }

  ngOnChanges() {
    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    switch (this.property) {
      case 'bug':
        this.propertyValue = this.diff.getBug();
        break;
      case 'description':
        this.propertyValue = this.diff.getDescription();
        break;
      default:
        throw new Error('Unsupported property');
    }
  }

  // Save the new value of property and update Diff
  savePropertyValue(): void {
    switch (this.property) {
      case 'bug':
        this.diff.setBug(this.propertyValue);
        break;
      case 'description':
        this.diff.setDescription(this.propertyValue);
        break;
      default:
        throw new Error('Unsupported property');
    }

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('Saved');
    }, () => {
      this.notificationService.error('Error');
    });
  }
}
