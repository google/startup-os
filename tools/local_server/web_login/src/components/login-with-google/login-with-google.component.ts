import { Component } from '@angular/core';
import { FirebaseService } from '@/services';

@Component({
  selector: 'login-with-google',
  templateUrl: './login-with-google.component.html',
  styleUrls: ['./login-with-google.component.scss']
})
export class LoginWithGoogleComponent {
  constructor(public firebaseService: FirebaseService) { }
}
