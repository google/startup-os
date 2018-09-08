import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '@/shared';

@Component({
  selector: 'cr-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  constructor(private authService: AuthService, private router: Router) {
    this.authService.angularFireAuth.authState.subscribe(auth => {
      if (auth) {
        this.router.navigate(['']);
      }
    });
  }

  loginUsingGoogle(): void {
    this.authService.loginWithGoogle().then(() => {
      this.router.navigate(['/diffs']);
    });
  }
}
