import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';

import { FirebaseStateService } from './firebase-state.service';

@Injectable()
export class AuthService {
  isOnline: boolean;
  userEmail: string;

  constructor(
    public angularFireAuth: AngularFireAuth,
    private firebaseStateService: FirebaseStateService,
  ) {
    this.angularFireAuth.authState.subscribe(userData => {
      this.isOnline = !!userData;
      if (this.isOnline) {
        this.userEmail = userData.email;
        this.firebaseStateService.connectDiffs();
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
    this.firebaseStateService.destroy();
    return this.angularFireAuth.auth.signOut();
  }
}
