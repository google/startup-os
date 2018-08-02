import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Diff, Reviewer } from '@/shared/proto';
import {
  AuthService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { AddUserDialogComponent } from '../add-user-dialog';
import { DiffHeaderService } from '../diff-header.service';

@Component({
  selector: 'diff-header-content',
  templateUrl: './diff-header-content.component.html',
  styleUrls: ['./diff-header-content.component.scss'],
  providers: [DiffHeaderService],
})
export class DiffHeaderContentComponent implements OnInit {
  textareaControl: FormControl = new FormControl();
  @Input() diff: Diff;

  constructor(
    public authService: AuthService,
    public firebaseService: FirebaseService,
    public notificationService: NotificationService,
    public diffHeaderService: DiffHeaderService,
    public dialog: MatDialog,
  ) {
    // Save description when it's changed
    this.textareaControl.valueChanges
      .pipe(
        // But not save the same value
        distinctUntilChanged(),
        // Do not save more than once a second
        debounceTime(1000),
      )
      .subscribe((description: string) => {
        this.diff.setDescription(description);
        this.firebaseService.updateDiff(this.diff).subscribe();
      });
  }

  ngOnInit() {
    this.textareaControl.setValue(this.diff.getDescription());
  }

  changeAttention(email: string): void {
    // Get reviewer
    const reviewer: Reviewer = this.diffHeaderService.getReviewer(
      this.diff,
      email,
    );
    if (!reviewer) {
      throw new Error('Reviewer not found');
    }

    // Change attention of the reviewer
    this.diffHeaderService.changeAttention(reviewer);

    // Send changes to firebase
    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      const username: string = this.authService
        .getUsername(reviewer.getEmail());
      const message: string = reviewer.getNeedsAttention() ?
        `Attention of ${username} is requested` :
        `Attention of ${username} is canceled`;
      this.notificationService.success(message);
    }, () => {
      this.notificationService.error("Attention isn't changed");
    });
  }

  // Is the email is present in the CC list?
  isCcAlreadyPresent(diff: Diff, email: string): boolean {
    for (const cc of diff.getCcList()) {
      if (cc === email) {
        return true;
      }
    }
    return false;
  }

  // Open "add reviewer" dialog
  addReviewer(): void {
    this.openDialog().afterClosed().subscribe((email: string) => {
      if (email && !this.diffHeaderService.getReviewer(this.diff, email)) {
        const reviewer = new Reviewer();
        reviewer.setEmail(email);
        reviewer.setNeedsAttention(true);
        reviewer.setApproved(false);
        this.diff.addReviewer(reviewer);
        this.saveUserToFirebase(email);
      }
    });
  }

  // Open "add cc" dialog
  addCC(): void {
    this.openDialog().afterClosed().subscribe((email: string) => {
      if (email && !this.isCcAlreadyPresent(this.diff, email)) {
        this.diff.addCc(email);
        this.saveUserToFirebase(email);
      }
    });
  }

  openDialog(): MatDialogRef<AddUserDialogComponent> {
    return this.dialog.open(AddUserDialogComponent);
  }

  removeFromReviewerList(email: string): void {
    this.diffHeaderService.removeFromReviewerList(this.diff, email);
    this.removeUserFromFirebase(email);
  }

  removeFromCcList(email: string): void {
    this.diffHeaderService.removeFromCcList(this.diff, email);
    this.removeUserFromFirebase(email);
  }

  saveUserToFirebase(email: string): void {
    this.updateUserListInFirebase(email, 'added');
  }

  removeUserFromFirebase(email: string): void {
    this.updateUserListInFirebase(email, 'removed');
  }

  // To make code more DRY
  // saveUserToFirebase and removeUserFromFirebase use the method
  updateUserListInFirebase(email: string, action: string): void {
    const username: string = this.authService.getUsername(email);

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success(username + ' is ' + action);
    }, () => {
      this.notificationService.error(username + " isn't " + action);
    });
  }
}
