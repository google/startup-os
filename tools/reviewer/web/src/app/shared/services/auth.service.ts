import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';
import { User } from 'firebase/app';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class AuthService {
  user: string;

  constructor(public af: AngularFireAuth) {
    this.af.authState.subscribe(userData => {
      this.user = userData ? userData.email.split('@')[0] : undefined;
    });
  }

  getUser(): Observable<User> {
    return this.af.authState;
  }

  loginWithGoogle() {
    return this.af.auth.signInWithPopup(
      new firebase.auth.GoogleAuthProvider()
    );
  }

  logout() {
    return this.af.auth.signOut();
  }
}
