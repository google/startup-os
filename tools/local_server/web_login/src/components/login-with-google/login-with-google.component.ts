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
      const accessToken = result.credential.accessToken;
      const refreshToken = result.user.refreshToken;
      this.getJwtToken(accessToken, refreshToken);
    });
  }

  getJwtToken(accessToken: string, refreshToken: string): void {
    this.firebaseService.access.auth.currentUser.getIdToken(true)
      .then(token => {
        this.sendToServer(accessToken, refreshToken, token);
      });
  }

  sendToServer(
    accessToken: string,
    refreshToken: string,
    jwtToken: string
  ): void {
    this.http.post(`http://localhost:${port}/token`, {
      accessToken: accessToken,
      refreshToken: refreshToken,
      jwtToken: jwtToken,
      projectId: config.projectId,
    }).subscribe(() => { });
  }
}
