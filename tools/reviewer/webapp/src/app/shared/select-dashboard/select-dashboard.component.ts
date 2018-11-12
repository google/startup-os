import { Component } from '@angular/core';

import { UserService } from '@/core';
import { SelectDashboardService } from '@/core/services';

@Component({
  selector: 'select-dashboard',
  templateUrl: './select-dashboard.component.html',
  styleUrls: ['./select-dashboard.component.scss'],
})
export class SelectDashboardComponent {
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
}
