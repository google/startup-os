import { Component, HostListener } from '@angular/core';
import { MatDialogRef } from '@angular/material';

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

    // Example: my_super-name@flip-flop.com
    const emailValidation: RegExp = /^[\w-.]+@[\w-]+\.[\w]+$/;
    return emailValidation.test(email.toLowerCase());
  }

  hideError() {
    this.isEmailInvalid = false;
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    // Hide error when user's typing something
    this.hideError();

    // If enter is pressed then return data
    if (event.key === 'Enter') {
      this.add();
    }
  }
}
