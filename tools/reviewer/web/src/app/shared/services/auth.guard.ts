import { AuthService } from '@/shared/services/auth.service';
import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { Observable } from 'rxjs';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): Observable<boolean> {
    // There is a bug.
    // If you open the app without internet connection,
    // you don't see any loadings or error message.
    // You see nothing, just white screen forever.
    // TODO: do something with that
    return this.authService.angularFireAuth.authState.map(user => {
      const isAuthorized: boolean = !!user;
      if (!isAuthorized) {
        this.router.navigate(['/login']);
      }
      return isAuthorized;
    });
  }
}
