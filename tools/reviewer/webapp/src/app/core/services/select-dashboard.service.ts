import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';

import { Diff } from '@/core/proto';

@Injectable()
export class SelectDashboardService {
  selectedEmail: string;
  uniqueUsers: string[] = [];
  dashboardChanges = new Subject<string>();

  constructor(private router: Router) { }

  // Creates user list
  addUniqueUsers(diff: Diff): void {
    this.addUniqueUser(diff.getAuthor().getEmail());
    for (const reviewer of diff.getReviewerList()) {
      this.addUniqueUser(reviewer.getEmail());
    }
    for (const cc of diff.getCcList()) {
      this.addUniqueUser(cc);
    }
  }

  // Opens dashboard with the email
  selectDashboard(email: string): void {
    if (this.isDashboardInit) {
      this.router.navigate(['diffs'], { queryParams: { email: email } });
      this.selectedEmail = email;
      this.dashboardChanges.next(email);
    }
  }

  isDashboardInit(): boolean {
    return this.uniqueUsers.length > 0;
  }

  private addUniqueUser(email: string): void {
    if (email && this.uniqueUsers.indexOf(email) === -1) {
      this.uniqueUsers.push(email);
    }
  }

  refresh(): void {
    this.uniqueUsers = [];
  }
}
