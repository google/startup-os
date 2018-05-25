import { Diff } from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-editable-property',
  templateUrl: './editable-property.component.html',
  styleUrls: ['./editable-property.component.scss']
})
export class EditablePropertyComponent {
  @Input() diff: Diff;
  // Property name in Diff, e.g 'bug' or 'description'.
  // The property value is stored in propertyValue
  @Input() property: string;
  @Input() editable: boolean = true;
  @Output() onUpdateDiff = new EventEmitter<Diff>();

  // To show editable fields
  showEditableProperty = false;

  // Following variable is used in editing the fields
  propertyValue: string = '';

  ngOnChanges() {
    this.getPropertyValue();
  }

  // Get property value from the Diff
  getPropertyValue(): void {
    this.propertyValue = this.diff[this.property];
  }

  // Save the new value of property and update Diff
  savePropertyValue(): void {
    this.diff[this.property] = this.propertyValue;
    this.onUpdateDiff.emit(this.diff);
  }
}
