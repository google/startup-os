import { Component, Input, OnChanges } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';

import { Diff, Reviewer } from '@/shared/proto';
import { AuthService, DiffUpdateService, HighlightService } from '@/shared/services';
import { AddUserDialogComponent } from '../add-user-dialog';
import { DiffHeaderService } from '../diff-header.service';

// The component implements content of the header
// How it looks: https://i.imgur.com/TgqzvTW.jpg
@Component({
  selector: 'diff-header-content',
  templateUrl: './diff-header-content.component.html',
  styleUrls: ['./diff-header-content.component.scss'],
  providers: [DiffHeaderService],
})
export class DiffHeaderContentComponent implements OnChanges {
  description: string = '';
  isDescriptionEditMode: boolean = false;
  isReviewersHovered: boolean = false;
  isCCHovered: boolean = false;

  @Input() diff: Diff;

  constructor(
    public dialog: MatDialog,
    public authService: AuthService,
    public diffUpdateService: DiffUpdateService,
    public diffHeaderService: DiffHeaderService,
    public highlightService: HighlightService,
  ) { }

  ngOnChanges() {
    this.description = this.diff.getDescription();
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
    const username: string = this.authService.getUsername(reviewer.getEmail());
    this.diffUpdateService.updateAttention(this.diff, username);
  }

  // Is the email present in the CC list?
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

  updateUserListInFirebase(email: string, action: string): void {
    const username: string = this.authService.getUsername(email);
    this.diffUpdateService.customUpdate(this.diff, username + ' is ' + action);
  }

  startDescriptionEditMode(): void {
    this.isDescriptionEditMode = true;
  }

  stopDescriptionEditMode(): void {
    this.description = this.diff.getDescription();
    this.isDescriptionEditMode = false;
  }

  // Get description, where urls are highlighted as a link, and html escaped
  getParsedDescription(): string {
    // The RegExp example is taken from here:
    // https://stackoverflow.com/a/3809435

    // tslint:disable-next-line
    const urlRegExp: RegExp = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/g;

    // Thing is we need to escape scecial html chars, but a url contain the chars.
    // So we need to highlight urls and escape special chars separately.
    // Implementation: find a url, highlight it, make all text before the url escaped,
    // go to the next url.
    let parsedDescription: string = '';
    let clippedDescription: string = this.description;

    // Get all urls in the description
    const urls: RegExpMatchArray = this.description.match(urlRegExp);
    if (urls) {
      for (const url of this.description.match(urlRegExp)) {
        const linkIndex: number = clippedDescription.search(urlRegExp);
        // All text before the url
        const commonText: string = clippedDescription.substr(0, linkIndex);
        parsedDescription +=
          this.highlightService.htmlSpecialChars(commonText) + // Escape html
          url.link(url); // Hightlight the url
        // Cut the part, which we already parsed
        clippedDescription = clippedDescription.substr(
          linkIndex + url.length,
          clippedDescription.length - (linkIndex + url.length),
        );
      }
      // Don't forget about text after last found url
      parsedDescription += this.highlightService.htmlSpecialChars(clippedDescription);

      return parsedDescription;
    } else {
      // Description doesn't contain any links

      // Return a placeholder, if description is empty
      const placeholder: string = 'Description...';
      return this.description ? this.description : placeholder;
    }
  }

  saveDescription(): void {
    this.diff.setDescription(this.description);
    this.diffUpdateService.updateDescription(this.diff);
    this.isDescriptionEditMode = false;
  }
}
