// TODO: delete the service, when binary from firebase will be available.

import { Injectable } from '@angular/core';
import { AngularFireDatabase, AngularFireObject } from 'angularfire2/database';
import { Observable } from 'rxjs/Observable';

import { JsonDiff } from './json-diff.interface';

@Injectable()
export class FirebaseJsonService {
  constructor(private db: AngularFireDatabase) { }

  getDiffs(): Observable<any> {
    return this.db.object('diffs').valueChanges();
  }

  getDiff(id: string): Observable<JsonDiff> {
    const diff: AngularFireObject<JsonDiff> = this.db.object('diffs/' + id);
    return diff.valueChanges();
  }

  updateDiff(diff: JsonDiff): Observable<void> {
    return new Observable(observer => {
      this.db
        .object('diffs/' + diff.number)
        .update(diff)
        .then(() => {
          observer.next();
        })
        .catch(err => {
          observer.error();
        });
    });
  }
}
