import {
  Diff,
  FirebaseService,
  NotificationService,
  ProtoService,
  Status
} from '@/shared';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-bug',
  templateUrl: './bug.component.html',
  styleUrls: ['./bug.component.scss']
})
export class BugComponent {
  @Input() diff: Diff;
  @Output() onUpdateDiff = new EventEmitter<Diff>();

  bug: string = '';

  editable: boolean = true;
  notEditableStatus: Array<number> = [Status.SUBMITTED, Status.REVERTED];

  constructor(
    private protoService: ProtoService,
    private firebaseService: FirebaseService,
    private notify: NotificationService
  ) {}

  ngOnChanges() {
    this.getBug();
    this.editable = !this.notEditableStatus.includes(this.diff.status);
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
