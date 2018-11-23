import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';

import { FirebaseStateService } from './firebase-state.service';
import { UserService } from './user.service';

@Injectable()
export class AuthService {
  isOnline: boolean;

  constructor(
    private router: Router,
    private angularFireAuth: AngularFireAuth,
    private firebaseStateService: FirebaseStateService,
    private userService: UserService,
  ) {
    this.angularFireAuth.authState.subscribe(userData => {
      this.isOnline = !!userData;
      if (this.isOnline) {
        this.userService.email = userData.email;
        this.firebaseStateService.connectDiffs();
      } else {
        this.router.navigate(['/login']);
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
