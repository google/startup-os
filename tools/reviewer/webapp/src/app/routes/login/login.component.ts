import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '@/core';

@Component({
  selector: 'cr-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) { }

  loginUsingGoogle(): void {
    this.authService.logInWithGoogle().subscribe(() => {
      this.router.navigate(['/diffs']);
    });
  }
}
