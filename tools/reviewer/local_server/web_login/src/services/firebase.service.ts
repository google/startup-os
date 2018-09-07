import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';

@Injectable()
export class FirebaseService {
  isLoading: boolean = false;
  
  constructor(
    public access: AngularFireAuth,
  ) {

    this.access.authState.subscribe(() => {
      this.isLoading = false;
    });
  }

  login(callback?: (result: any) => void): void {
    this.isLoading = true;

    var provider = new firebase.auth.GoogleAuthProvider();
    provider.addScope('https://www.googleapis.com/auth/datastore');
    provider.addScope('https://www.googleapis.com/auth/cloud-platform');
    const signInPromise = this.access.auth.signInWithPopup(provider);

    if (callback) {
      signInPromise.then(result => {
        callback(result);
      });
    }
  }

  logout(): void {
    this.access.auth.signOut();
  }
}
