import { Injectable } from '@angular/core';
import {
  AngularFirestore,
  AngularFirestoreCollection,
  DocumentChangeAction,
  Action,
  DocumentSnapshot,
} from '@angular/fire/firestore';
import { Observable, Subscriber } from 'rxjs';
import { map } from 'rxjs/operators';

import { Diff } from '@/core/proto';
import { EncodingService } from './encoding.service';
import { UserService } from './user.service';

interface FirebaseElement {
  proto: string;
}

@Injectable()
export class FirebaseService {
  private diffs: AngularFirestoreCollection<FirebaseElement>;

  constructor(
    private db: AngularFirestore,
    private encodingService: EncodingService,
    private userService: UserService,
  ) {
    this.diffs = this.db.collection('reviewer/data/diff');
  }

  getDiffs(): Observable<Diff[]> {
    return this.diffs
      .snapshotChanges().pipe(
        map((actions: DocumentChangeAction<FirebaseElement>[]) => {
          return actions.map((action: DocumentChangeAction<FirebaseElement>) => {
            const firebaseElement: FirebaseElement = action.payload.doc.data();
            return this.convertFirebaseElementToDiff(firebaseElement);
          });
        }),
    );
  }

  getDiff(id: string): Observable<Diff> {
    return this.diffs
      .doc(id)
      .snapshotChanges().pipe(
        map((action: Action<DocumentSnapshot<FirebaseElement>>) => {
          const firebaseElement: FirebaseElement = action.payload.data();
          if (firebaseElement === undefined) {
            // Diff not found
            return;
          }
          return this.convertFirebaseElementToDiff(firebaseElement);
        }),
    );
  }

  updateDiff(diff: Diff): Observable<void> {
    // Unix timestamp in milliseconds
    const currentTimestampMs: number = Date.now();
    diff.setModifiedTimestamp(currentTimestampMs);
    diff.setModifiedBy(this.userService.email);

    return new Observable((observer: Subscriber<void>) => {
      this.diffs
        .doc(diff.getId().toString())
        .update(this.convertDiffToFirebaseElement(diff))
        .then(() => observer.next())
        .catch(() => observer.error());
    });
  }

  removeDiff(id: string): Observable<void> {
    return new Observable((observer: Subscriber<void>) => {
      this.diffs
        .doc(id)
        .delete()
        .then(() => observer.next())
        .catch(() => observer.error());
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

  private convertFirebaseElementToDiff(firebaseElement: FirebaseElement): Diff {
    // Convert firebaseElement to binary
    const binary: Uint8Array = this.encodingService
      .decodeBase64StringToUint8Array(firebaseElement.proto);
    // Convert binary to diff
    const diff: Diff = Diff.deserializeBinary(binary);

    return diff;
  }
}
