import { AuthService } from '@/core';
import { Component } from '@angular/core';

@Component({
  selector: 'cr-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
})
export class ToolbarComponent {
  constructor(public authService: AuthService) { }

  logout(): void {
    this.authService.logout();
  }
}
