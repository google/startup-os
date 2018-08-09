import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './shared';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {
    this.authService.angularFireAuth.authState.subscribe(auth => {
      if (!auth) {
        this.router.navigate(['/login']);
      }
    });
  }
}
