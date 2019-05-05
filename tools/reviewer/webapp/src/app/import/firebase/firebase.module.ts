import { FactoryProvider, NgModule } from '@angular/core';
import { AngularFireModule, FirebaseOptionsToken } from '@angular/fire';
import { AngularFireAuth, AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireDatabase } from '@angular/fire/database';
import { AngularFirestoreModule } from '@angular/fire/firestore';
import firebase from '@firebase/app';

import { FirebaseConfig } from '@/core/proto';

const CustomFirebaseInit: FactoryProvider = {
  provide: FirebaseOptionsToken,
  deps: [],
  useFactory: () => {
    const firebaseConfig: FirebaseConfig.AsObject = window['firebaseConfig'];
    return firebase.initializeApp(firebaseConfig);
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
