import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './shared';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {
    this.authService.af.authState.subscribe(auth => {
      if (auth === null) {
        this.router.navigate(['login']);
      }
    });
  }
}
