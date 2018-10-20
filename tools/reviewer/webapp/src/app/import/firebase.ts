import { AngularFireModule } from 'angularfire2';
import { AngularFireAuth, AngularFireAuthModule } from 'angularfire2/auth';
import { AngularFireDatabase } from 'angularfire2/database';
import { AngularFirestoreModule } from 'angularfire2/firestore';

import { config } from '../../environments/firebase';

export const FirebaseImports = [
  AngularFireModule.initializeApp(config),
  AngularFirestoreModule,
  AngularFireAuthModule,
];

export const FirebaseServices = [
  AngularFireDatabase,
  AngularFireAuth,
];
