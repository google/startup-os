import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';

@Injectable()
export class AuthService {
  isOnline: boolean;
  userEmail: string;

  constructor(public angularFireAuth: AngularFireAuth) {
    this.angularFireAuth.authState.subscribe(userData => {
      this.isOnline = !!userData;
      if (userData) {
        this.userEmail = userData.email;
      }
    });
  }

  getUsername(userEmail: string): string {
    return userEmail.split('@')[0];
  }

  loginWithGoogle() {
    return this.angularFireAuth.auth.signInWithPopup(
      new firebase.auth.GoogleAuthProvider(),
    );
  }

  logout() {
    return this.angularFireAuth.auth.signOut();
  }
}
