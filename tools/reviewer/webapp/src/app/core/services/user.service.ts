import { Injectable } from '@angular/core';

@Injectable()
export class UserService {
  email: string;

  getUsername(userEmail: string): string {
    return userEmail.split('@')[0];
  }
}
