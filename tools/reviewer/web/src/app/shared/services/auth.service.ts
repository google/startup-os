import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';
import { User } from 'firebase/app';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class AuthService {
  user: string;

  constructor(public angularFireAuth: AngularFireAuth) {
    this.angularFireAuth.authState.subscribe(userData => {
      this.user = userData ? userData.email.split('@')[0] : undefined;
    });
  }

  getUser(): Observable<User> {
    return this.angularFireAuth.authState;
  }

  loginWithGoogle() {
    return this.angularFireAuth.auth.signInWithPopup(
      new firebase.auth.GoogleAuthProvider()
    );
  }

  logout() {
    return this.angularFireAuth.auth.signOut();
  }
}
