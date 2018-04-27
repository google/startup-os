import { AuthService } from '@/shared';
import { Component, NgZone } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
    private zone: NgZone
  ) {}

  logout(): void {
    this.authService.logout();
  }

  navigateToReviewsPanel(): void {
    this.zone.run(() => {
      this.router.navigate(['diffs', {}]);
    });
  }
}
