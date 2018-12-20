import { FactoryProvider, NgModule } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import firebase from '@firebase/app';
import { AngularFireModule, FirebaseApp } from 'angularfire2';
import { AngularFireAuth, AngularFireAuthModule } from 'angularfire2/auth';
import { AngularFireDatabase } from 'angularfire2/database';
import { AngularFirestoreModule } from 'angularfire2/firestore';

const CustomFirebaseInit: FactoryProvider = {
  provide: FirebaseApp,
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
