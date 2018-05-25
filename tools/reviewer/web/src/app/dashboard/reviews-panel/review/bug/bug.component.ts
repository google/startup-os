import { Diff } from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

// Bug component is used to display a link to github
// issue related to this Diff

@Component({
  selector: 'app-bug',
  templateUrl: './bug.component.html',
  styleUrls: ['./bug.component.scss']
})
export class BugComponent {
  @Input() diff: Diff;
  @Input() editable: boolean = true;
  @Output() onUpdateDiff = new EventEmitter<Diff>();

  bug: string = '';

  ngOnChanges() {
    this.getBug();
  }

  // Get bug property from Diff
  getBug(): void {
    this.bug = this.diff.bug;
  }

  // Save bug property from edited field and update the Diff
  saveBug(): void {
    this.diff.bug = this.bug;
    this.onUpdateDiff.emit(this.diff);
  }
}
