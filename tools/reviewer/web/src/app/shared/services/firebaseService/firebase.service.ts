import { Diff } from '@/shared';
import { Injectable } from '@angular/core';
import {
  AngularFireAction,
  AngularFireDatabase,
  AngularFireObject,
  DatabaseSnapshot
} from 'angularfire2/database';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class FirebaseService {
  constructor(private db: AngularFireDatabase) {}

  getDiffs(): Observable<any> {
    return this.db.object('diffs').valueChanges();
  }

  getDiff(id: string): Observable<Array<Diff>> {
    const obj: AngularFireObject<Array<Diff>> = this.db.object('diffs/' + id);
    return obj.valueChanges();
  }

  updateDiff(diff: Diff): Promise<void> {
    return this.db
      .object('diffs/' + diff.number)
      .update(diff)
      .then(() => {
        // TODO Notify of comment saved Success
      })
      .catch(err => {
        // TODO Notify of error
        console.log(err);
      });
  }
}
