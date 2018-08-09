import { Component, HostListener } from '@angular/core';
import { MatDialogRef } from '@angular/material';

// The dialog appears when "Add new user" button is pushed.
// How it looks: https://i.imgur.com/HnZnth5.jpg
@Component({
  selector: 'add-user-dialog',
  templateUrl: './add-user-dialog.component.html',
  styleUrls: ['./add-user-dialog.component.scss'],
})
export class AddUserDialogComponent {
  isEmailInvalid: boolean = false;
  email: string;

  constructor(public dialogRef: MatDialogRef<AddUserDialogComponent>) { }

  add(): void {
    if (this.isEmailValid(this.email)) {
      this.dialogRef.close(this.email);
    } else {
      this.isEmailInvalid = true;
    }
  }

  close(): void {
    this.dialogRef.close();
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

  hideError() {
    this.isEmailInvalid = false;
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    // Hide error when user is typing something
    this.hideError();

    // If enter is pressed then return data
    if (event.key === 'Enter') {
      this.add();
    }
  }
}
