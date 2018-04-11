import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';

import { port } from '@/bootstrap/environments/port';
import { config } from '@/bootstrap/environments/firebase';

@Injectable()
export class FirebaseService {
  isLoading: boolean = false;

  constructor(
    public access: AngularFireAuth,
    public http: Http
  ) {

    this.access.authState.subscribe(() => {
      this.isLoading = false;
    });
  }

  login(): void {
    this.isLoading = true;

    var provider = new firebase.auth.GoogleAuthProvider();
    provider.addScope('https://www.googleapis.com/auth/datastore');
    provider.addScope('https://www.googleapis.com/auth/cloud-platform');
    this.access.auth.signInWithPopup(provider)
      .then(result => {
        // Send token and projectId
        this.http.post(`http://localhost:${port}/token`, {
          token: result.credential.accessToken,
          projectId: config.projectId,
        }).subscribe(() => { });
      });
  }

  logout(): void {
    this.access.auth.signOut();
  }
}
