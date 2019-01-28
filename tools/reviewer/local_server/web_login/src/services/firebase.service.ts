import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import { Observable } from 'rxjs';
import * as firebase from 'firebase/app';

interface signInReturn {
  user: {
    refreshToken: string;
  };
}

@Injectable()
export class FirebaseService {
  isLoading: boolean = false;

  constructor(public access: AngularFireAuth) {

    this.access.authState.subscribe(() => {
      this.isLoading = false;
    });
  }

  login(): Observable<signInReturn> {
    this.isLoading = true;

    return new Observable(observer => {
      const provider: firebase.auth.GoogleAuthProvider = new firebase.auth.GoogleAuthProvider();
      const signInPromise: Promise<signInReturn> = this.access.auth.signInWithPopup(provider);

      signInPromise.then(result => {
        observer.next(result);
      });
    });
  }

  logout(): void {
    this.access.auth.signOut();
  }
}
