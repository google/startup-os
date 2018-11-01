import { Component } from '@angular/core';

import { UserService } from '@/core';
import { SelectDashboardService } from './select-dashboard.service';

@Component({
  selector: 'select-dashboard-popup',
  templateUrl: './select-dashboard-popup.component.html',
  styleUrls: ['./select-dashboard-popup.component.scss'],
})
export class SelectDashboardPopupComponent {
  isVisible: boolean = false;

  constructor(
    public userService: UserService,
    public selectDashboardService: SelectDashboardService,
  ) { }

  show(): void {
    this.isVisible = true;
  }

  hide(): void {
    this.isVisible = false;
  }

  openDashboard(email: string): void {
    this.selectDashboardService.selectDashboard(email);
    this.hide();
  }

  home(): void {
    this.openDashboard(this.userService.email);
  }
}
