import { AuthService } from '@/shared';
import { Component } from '@angular/core';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
})
export class ToolbarComponent {
  constructor(private authService: AuthService) { }

  logout(): void {
    this.authService.logout();
  }
}
