import { Injectable } from '@angular/core';
import {
  AngularFirestore,
  AngularFirestoreCollection,
} from 'angularfire2/firestore';
import * as firebase from 'firebase/app';
import { Observable } from 'rxjs/Observable';

import { Diff } from '@/shared/shell';

interface FirebaseElement {
  proto: firebase.firestore.Blob;
}

@Injectable()
export class FirebaseService {
  private diffs: AngularFirestoreCollection<FirebaseElement>;

  constructor(private db: AngularFirestore) {
    this.diffs = this.db.collection('diffs');
  }

  getDiffs(): Observable<Diff[]> {
    return this.diffs
      .snapshotChanges()
      .map(actions => {
        return actions.map(action => {
          const firebaseElement = action.payload.doc.data() as FirebaseElement;
          return this.converFirebaseElementToDiff(firebaseElement);
        });
      });
  }

  getDiff(id: string): Observable<Diff> {
    return this.diffs
      .doc(id)
      .snapshotChanges()
      .map(action => {
        const firebaseElement = action.payload.data() as FirebaseElement;
        return this.converFirebaseElementToDiff(firebaseElement);
      });
  }

  updateDiff(diff: Diff): Observable<void> {
    return new Observable(observer => {
      this.diffs
        .doc(diff.getId().toString())
        .update(this.convertDiffToFirebaseElement(diff))
        .then(() => {
          // Accessed
          observer.next();
        })
        .catch(() => {
          // Permission denied
          observer.error();
        });
    });
  }

  private convertDiffToFirebaseElement(diff: Diff): FirebaseElement {
    return {
      proto: firebase.firestore.Blob.fromUint8Array(diff.serializeBinary()),
    };
  }

  private converFirebaseElementToDiff(firebaseElement: FirebaseElement): Diff {
    // firebaseElement to binary
    const binary: Uint8Array = firebaseElement.proto.toUint8Array();
    // Convert binary to diff
    const diff: Diff = Diff.deserializeBinary(binary);

    return diff;
  }
}
