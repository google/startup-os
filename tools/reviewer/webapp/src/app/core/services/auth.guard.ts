import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { AngularFireAuth } from 'angularfire2/auth';
import { Observable } from 'rxjs';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private angularFireAuth: AngularFireAuth) { }

  canActivate(): Observable<boolean> {
    return this.angularFireAuth.authState.map(userData => {
      const isAuthorized: boolean = !!userData;
      return isAuthorized;
    });
  }
}
