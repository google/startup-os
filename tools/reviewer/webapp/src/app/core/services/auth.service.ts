import { Injectable } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
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

  logInWithGoogle(): Observable<void> {
    return new Observable(observer => {
      this.angularFireAuth.auth.signInWithPopup(
        new firebase.auth.GoogleAuthProvider(),
      )
        .then(() => observer.next())
        .catch(() => observer.error());
    });
  }

  logOut(): Observable<void> {
    this.firebaseStateService.destroy();
    return new Observable(observer => {
      this.angularFireAuth.auth.signOut()
        .then(() => observer.next())
        .catch(() => observer.error());
    });
  }
}
