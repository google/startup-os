import { AuthService } from '@/core';
import { Component } from '@angular/core';

import { SelectDashboardService, UserService } from '@/core/services';

@Component({
  selector: 'cr-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
})
export class ToolbarComponent {
  constructor(
    public authService: AuthService,
    public userService: UserService,
    public selectDashboardService: SelectDashboardService,
  ) { }

  logout(): void {
    this.authService.logOut().subscribe();
  }

  home(): void {
    this.selectDashboardService.selectDashboard(this.userService.email);
  }
}
