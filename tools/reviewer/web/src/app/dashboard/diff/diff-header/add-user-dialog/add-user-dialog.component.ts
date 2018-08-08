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

    // Javascript email regex from https://emailregex.com/
    const emailValidation: RegExp = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return emailValidation.test(email.toLowerCase());
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
