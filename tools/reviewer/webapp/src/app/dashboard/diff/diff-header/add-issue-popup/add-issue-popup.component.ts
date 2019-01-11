import { Component, EventEmitter, HostListener, Output } from '@angular/core';

@Component({
  selector: 'add-issue-popup',
  templateUrl: './add-issue-popup.component.html',
  styleUrls: ['./add-issue-popup.component.scss'],
})
export class AddIssuePopupComponent {
  email: string;
  isVisible: boolean = false;

  @Output() returnEmailEmitter = new EventEmitter<string>();

  constructor() {
    this.reset();
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if (!this.isVisible) {
      return;
    }

    // If Enter is pressed then add user
    if (event.key === 'Enter') {
      this.add();
    }

    // If Esc is pressed then close popup
    if (event.key === 'Escape') {
      this.close();
    }
  }

  open(): void {
    this.isVisible = true;
  }

  close(): void {
    this.reset();
  }

  add(): void {
    this.returnEmailEmitter.emit(this.email);
    this.close();
  }

  reset(): void {
    this.email = '';
    this.isVisible = false;
  }
}
