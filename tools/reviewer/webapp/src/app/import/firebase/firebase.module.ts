import { FactoryProvider, NgModule } from '@angular/core';
import { AngularFireModule, FirebaseOptionsToken } from '@angular/fire';
import { AngularFireAuth, AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFireDatabase } from '@angular/fire/database';
import { AngularFirestoreModule } from '@angular/fire/firestore';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import firebase from '@firebase/app';

const CustomFirebaseInit: FactoryProvider = {
  provide: FirebaseOptionsToken,
  deps: [],
  useFactory: () => {
    const reviewerConfig = platformBrowserDynamic().injector.get('reviewerConfig');
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
