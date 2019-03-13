import { Injectable } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { Router } from '@angular/router';
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
      this.isOnline = !!userData && !!userData.email;
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

  loginWithGoogle(): Promise<any> {
    return this.angularFireAuth.auth.signInWithPopup(
      new firebase.auth.GoogleAuthProvider(),
    );
  }

  logout(): Promise<any> {
    this.firebaseStateService.destroy();
    return this.angularFireAuth.auth.signOut();
  }
}
