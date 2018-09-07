import { Component } from '@angular/core';
import { FirebaseService } from '@/services';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';

import { port } from '@/bootstrap/environments/port';
import { config } from '@/bootstrap/environments/firebase';

@Component({
  selector: 'login-with-google',
  templateUrl: './login-with-google.component.html',
  styleUrls: ['./login-with-google.component.scss']
})
export class LoginWithGoogleComponent {
  constructor(
    public firebaseService: FirebaseService,
    private http: Http,
  ) {}

  login(): void {
    this.firebaseService.login(result => {
      this.getJwtToken(result.user.refreshToken);
    });
  }

  getJwtToken(refreshToken: string): void {
    this.firebaseService.access.auth.currentUser.getIdToken(true)
      .then(jwtToken => {
        this.sendToServer(jwtToken, refreshToken);
      });
  }

  sendToServer(
    jwtToken: string,
    refreshToken: string
  ): void {
    this.http.post(`http://localhost:${port}/token`, {
      projectId: config.projectId,
      apiKey: config.apiKey,
      jwtToken: jwtToken,
      refreshToken: refreshToken,
    }).subscribe(() => { });
  }
}
