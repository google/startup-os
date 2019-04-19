import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable()
export class AuthMockService {
  isOnline: boolean;

  constructor() {
    this.isOnline = true;
  }

  getUsername(userEmail: string): string {
    return 'testuser';
  }

  logInWithGoogle(): Observable<void> {
    this.isOnline = true;
    return of();
  }

  logOut(): Observable<void> {
    this.isOnline = false;
    return of();
  }
}
