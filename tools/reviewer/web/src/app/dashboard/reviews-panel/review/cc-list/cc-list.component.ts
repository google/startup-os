import { Component, HostListener, Input, OnInit } from '@angular/core';

import {
  AuthService,
  FirebaseService,
  NotificationService,
} from '@/shared/services';
import { Diff } from '@/shared/shell';

// Implementation of CC list on UI
@Component({
  selector: 'cc-list',
  templateUrl: './cc-list.component.html',
  styleUrls: ['./cc-list.component.scss'],
})
export class CCListComponent implements OnInit {
  usernames: string[] = [];
  usernameInput: string = '';
  isMouseHovered: boolean = false;
  isEditing: boolean = false;

  @Input() diff: Diff;

  constructor(
    private firebaseService: FirebaseService,
    private notificationService: NotificationService,
    private authService: AuthService,
  ) { }

  ngOnInit() {
    this.usernames = this.diff.getCcList()
      .map(email => this.authService.getUsername(email));
  }

  createUsernameInput(): void {
    this.isEditing = true;
    this.usernameInput = this.usernames.join(', ');
  }

  closeEditing(): void {
    this.isEditing = false;
  }

  // Get emails from input and send them to firebase
  saveCCInput(): void {
    const ccList: string[] = [];
    this.usernames = [];

    this.usernameInput
      .split(',')
      .map(username => username.trim())
      .filter(username => username.length)
      .forEach(username => {
        this.usernames.push(username);

        let email = this.getCCWithTheUsername(username);
        if (!email) {
          email = username + '@gmail.com';
        }
        ccList.push(email);
      });

    this.diff.setCcList(ccList);

    this.firebaseService.updateDiff(this.diff).subscribe(() => {
      this.notificationService.success('CC list saved');
    }, () => {
      this.notificationService.error("CC list can't be saved");
    });

    this.isEditing = false;
  }

  getCCWithTheUsername(username: string): string {
    for (const email of this.diff.getCcList()) {
      const usernameFromList: string = this.authService
        .getUsername(email);

      if (usernameFromList === username) {
        return email;
      }
    }
  }

  @HostListener('mouseenter') onMouseEnter() {
    this.isMouseHovered = true;
  }

  @HostListener('mouseleave') onMouseLeave() {
    this.isMouseHovered = false;
  }
}
