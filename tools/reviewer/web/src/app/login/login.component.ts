import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '@/shared';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  constructor(private authService: AuthService, private router: Router) {}

  loginUsingGoogle(): void {
    this.authService.loginWithGoogle().then(() => {
      this.router.navigate(['']);
    });
  }
}
