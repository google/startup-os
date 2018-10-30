import { Component, EventEmitter, HostListener, Output } from '@angular/core';

@Component({
  selector: 'add-user-popup',
  templateUrl: './add-user-popup.component.html',
  styleUrls: ['./add-user-popup.component.scss'],
})
export class AddUserPopupComponent {
  isEmailInvalid: boolean;
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

    // Hide error when user is typing something
    this.hideError();

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
    if (this.isEmailValid(this.email)) {
      this.returnEmailEmitter.emit(this.email);
      this.close();
    } else {
      this.isEmailInvalid = true;
    }
  }

  isEmailValid(email: string): boolean {
    if (!email) {
      return false;
    }
    // <something>@<something>
    // e.g. "username@domain.com"
    const emailValidation: RegExp = /^.+?@.+?$/;
    return emailValidation.test(email);
  }

  hideError(): void {
    this.isEmailInvalid = false;
  }

  reset(): void {
    this.email = '';
    this.isEmailInvalid = false;
    this.isVisible = false;
  }
}
