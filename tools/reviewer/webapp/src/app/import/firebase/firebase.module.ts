import { FactoryProvider, NgModule } from '@angular/core';
import { AngularFireModule, FirebaseOptionsToken } from '@angular/fire';
import { AngularFireAuth, AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireDatabase } from '@angular/fire/database';
import { AngularFirestoreModule } from '@angular/fire/firestore';
import firebase from '@firebase/app';

import { Config } from './config.interface';

const CustomFirebaseInit: FactoryProvider = {
  provide: FirebaseOptionsToken,
  deps: [],
  useFactory: () => {
    const reviewerConfig: Config = window['reviewerConfig'];
    return firebase.initializeApp(reviewerConfig);
  },
};

@NgModule({
  imports: [
    AngularFireModule,
  ],
  exports: [
    AngularFirestoreModule,
    AngularFireAuthModule,
  ],
  providers: [
    AngularFireDatabase,
    AngularFireAuth,
    CustomFirebaseInit,
  ],
})
export class FirebaseModule { }
