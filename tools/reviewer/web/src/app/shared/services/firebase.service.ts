import { Injectable } from '@angular/core';
import {
  AngularFirestore,
  AngularFirestoreCollection,
} from 'angularfire2/firestore';
import { Observable } from 'rxjs/Observable';

import { Diff } from '@/shared/proto';
import { EncodingService } from './encoding.service';

interface FirebaseElement {
  proto: string;
}

@Injectable()
export class FirebaseService {
  private diffs: AngularFirestoreCollection<FirebaseElement>;

  constructor(
    private db: AngularFirestore,
    private encodingService: EncodingService,
  ) {
    this.diffs = this.db.collection('reviewer/data/diff');
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
    // Convert diff to binary
    const binary: Uint8Array = diff.serializeBinary();
    // Convert binary to firebaseElement
    const firebaseElement: FirebaseElement = {
      proto: this.encodingService.encodeUint8ArrayToBase64String(binary),
    };

    return firebaseElement;
  }

  private converFirebaseElementToDiff(firebaseElement: FirebaseElement): Diff {
    // Convert firebaseElement to binary
    const binary: Uint8Array = this.encodingService
      .decodeBase64StringToUint8Array(firebaseElement.proto);
    // Convert binary to diff
    const diff: Diff = Diff.deserializeBinary(binary);

    return diff;
  }
}
